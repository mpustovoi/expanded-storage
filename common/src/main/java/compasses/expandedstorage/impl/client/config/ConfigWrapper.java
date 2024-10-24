package compasses.expandedstorage.impl.client.config;

import com.google.gson.JsonParseException;
import compasses.expandedstorage.impl.misc.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

// todo: move to ellemes-container-library.json (or explore toml?)
public abstract class ConfigWrapper {
    public static final Logger LOGGER = LogManager.getLogger(Utils.MOD_ID);

    public static void warnThrowableMessage(String message, Throwable throwable, Object... values) {
        LOGGER.warn(new FormattedMessage(message, values, throwable));
    }

    private final ConfigV0 config;
    private final Path oldConfigPath;

    private final Path configPath;

    public ConfigWrapper(Path configPath, Path oldConfigPath) {
        this.configPath = configPath;
        this.oldConfigPath = oldConfigPath;
        config = this.getConfig();
    }

    public final boolean isScrollingUnrestricted() {
        return !config.isScrollingRestricted();
    }

    public final boolean preferSmallerScreens() {
        return config.preferSmallerScreens();
    }

    public final boolean fitVanillaConstraints() {
        return config.fitVanillaConstraints();
    }

    public final ResourceLocation getPreferredScreenType() {
        return config.getScreenType();
    }

    private boolean isValidScreenType(ResourceLocation type) {
        return Utils.PAGINATED_SCREEN_TYPE.equals(type) || Utils.SCROLLABLE_SCREEN_TYPE.equals(type) || Utils.SINGLE_SCREEN_TYPE.equals(type);
    }

    public final void setPreferredScreenType(ResourceLocation type) {
        if (isValidScreenType(type) && type != config.getScreenType()) {
            config.setScreenType(type);
            this.saveConfig(config);
        }
    }

    /**
     * Should always return the raw old config text. If existing config is given return that else the parsed old config.
     */
    protected abstract ConfigV0 readOldConfig(String configLines, Path oldConfigPath);

    // protected final
    private ConfigV0 getConfig() {
        boolean triedLoadingOldConfig = false;
        boolean triedLoadingNewConfig = false;
        ConfigV0 config = null;
        if (Files.exists(configPath)) {
            triedLoadingNewConfig = true;
            config = this.loadConfig(configPath, ConfigV0.Factory.INSTANCE, false);
        }
        if (Files.exists(oldConfigPath)) {
            triedLoadingOldConfig = true;
            try (BufferedReader reader = Files.newBufferedReader(oldConfigPath)) {
                String configLines = reader.lines().collect(Collectors.joining());
                if (config == null) {
                    ConfigV0 oldConfig = this.readOldConfig(configLines, oldConfigPath);
                    if (oldConfig != null) {
                        config = oldConfig;
                        this.saveConfig(config);
                    }
                }
                this.backupFile(oldConfigPath, String.format("Failed to backup legacy Expanded Storage config, '%s'.", oldConfigPath.getFileName().toString()), configLines);
            } catch (IOException e) {
                if (config == null) {
                    LOGGER.warn("Failed to load legacy Expanded Storage Config, new default config will be used.", e);
                }
            }
        }
        if (config == null) {
            if (triedLoadingOldConfig || triedLoadingNewConfig) {
                LOGGER.warn("Could not load an existing config, Expanded Storage is using it's default config.");
            }
            config = new ConfigV0();
            this.saveConfig(config);
        }
        return config;
    }

    protected final <T extends Config> void saveConfig(T config) {
        try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            Map<String, Object> configValues = config.getConverter().toSource(config);
            configValues.put("config_version", config.getVersion());
            Utils.GSON.toJson(configValues, Utils.MAP_TYPE, Utils.GSON.newJsonWriter(writer));
        } catch (IOException e) {
            LOGGER.warn("Failed to save Expanded Storage's config.", e);
        }
    }

    // Tries to load a config file, returns null if loading fails.
    // Will need to be reworked to allow converting between ConfigV0 and ConfigV1
    // essentially converter will need to be decided in this method based on the value of "config_version"
    protected final <T extends Config> T convertToConfig(String lines, Converter<Map<String, Object>, T> converter, Path configPath) {
        try {
            Map<String, Object> configMap = Utils.GSON.fromJson(lines, Utils.MAP_TYPE);
            // Do not edit, gson returns a double, we want an int.
            int configVersion = Mth.floor((Double) configMap.getOrDefault("config_version", -1.0D));
            return this.convert(configMap, configVersion, converter);
        } catch (JsonParseException e) {
            String configFileName = configPath.getFileName().toString();
            ConfigWrapper.warnThrowableMessage("Failed to convert config, backing up config '{}'.", e, configFileName);
            this.backupFile(configPath, String.format("Failed to backup expanded storage config which failed to read, '%s'.%n", configFileName), lines);
            return null;
        }
    }

    protected final <A, B extends Config> B convert(A config, int configVersion, Converter<A, B> converter) {
        if (configVersion == converter.getSourceVersion()) {
            B returnValue = converter.fromSource(config);
            if (returnValue == null) {
                return null;
            }
            if (returnValue.getVersion() == converter.getTargetVersion()) {
                return returnValue;
            } else {
                throw new IllegalStateException(String.format("CODE ERROR: Converter converted to an invalid config, expected version %s, got %s.", converter.getTargetVersion(), returnValue.getVersion()));
            }
        } else {
            throw new IllegalStateException(String.format("CODE ERROR: Converter converted to an invalid config, expected version %s, got %s.", converter.getSourceVersion(), configVersion));
        }
    }

    protected final void backupFile(Path path, String failureMessage, String contents) {
        try {
            Path backupPath = path.resolveSibling(path.getFileName() + new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()) + ".backup");
            Files.move(path, backupPath);
        } catch (IOException e) {
            LOGGER.warn(failureMessage, e);
            if (contents != null) {
                LOGGER.warn(contents);
            }
        }
    }

    protected final <T extends Config> T loadConfig(Path configPath, Converter<Map<String, Object>, T> converter, boolean isLegacy) {
        try (BufferedReader reader = Files.newBufferedReader(configPath)) {
            String configLines = reader.lines().collect(Collectors.joining());
            return this.convertToConfig(configLines, converter, configPath);
        } catch (IOException e) {
            String configFileName = configPath.getFileName().toString();
            ConfigWrapper.warnThrowableMessage("Failed to read {}Expanded Storage config, '{}'.", e, isLegacy ? "legacy " : "", configFileName);
            e.printStackTrace();
            this.backupFile(configPath, String.format("Failed to backup %sExpanded Storage config, '%s'.%n", isLegacy ? "legacy " : "", configFileName), null);
        }
        return null;
    }
}
