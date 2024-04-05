package compasses.expandedstorage.impl;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import compasses.expandedstorage.impl.block.OpenableBlock;
import compasses.expandedstorage.impl.block.misc.BasicLockable;
import compasses.expandedstorage.impl.block.misc.CopperBlockHelper;
import compasses.expandedstorage.impl.misc.ClientboundUpdateRecipesMessage;
import compasses.expandedstorage.impl.misc.Utils;
import compasses.expandedstorage.impl.recipe.ConversionRecipeManager;
import compasses.expandedstorage.impl.recipe.ConversionRecipeReloadListener;
import compasses.expandedstorage.impl.registration.Content;
import compasses.expandedstorage.impl.registration.NamedValue;
import compasses.expandedstorage.impl.block.misc.ChestItemAccess;
import compasses.expandedstorage.impl.block.misc.GenericItemAccess;
import compasses.expandedstorage.impl.item.ChestBlockItem;
import compasses.expandedstorage.impl.item.ForgeChestMinecartItem;
import compasses.expandedstorage.impl.item.MiniStorageBlockItem;
import compasses.expandedstorage.impl.misc.ForgeCommonHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

@Mod("expandedstorage")
public final class ForgeMain {
    public ForgeMain(IEventBus modBus) {
        CommonMain.constructContent(new ForgeCommonHelper(), GenericItemAccess::new, BasicLockable::new,
                FMLLoader.getDist().isClient(), content -> registerContent(modBus, content),
                /*Base*/ false,
                /*Chest*/ ChestBlockItem::new, ChestItemAccess::new,
                /*Minecart Chest*/ ForgeChestMinecartItem::new,
                /*Old Chest*/
                /*Barrel*/ TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "barrels/wooden")),
                /*Mini Storage*/ MiniStorageBlockItem::new);
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new ConversionRecipeReloadListener()));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> CommonMain.platformHelper().sendConversionRecipesToClient(event.getPlayer(), ConversionRecipeManager.INSTANCE.getBlockRecipes(), ConversionRecipeManager.INSTANCE.getEntityRecipes()));

        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteractSpecific event) -> {
            InteractionResult result = CommonMain.interactWithEntity(event.getLevel(), event.getEntity(), event.getHand(), event.getTarget());
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        });

        modBus.addListener((RegisterEvent event) -> {
            event.register(Registries.MENU, helper -> {
                helper.register(Utils.HANDLER_TYPE_ID, CommonMain.platformHelper().getScreenHandlerType());
            });
        });

        modBus.addListener(this::registerPayloads);
    }

    @SubscribeEvent
    private void registerPayloads(RegisterPayloadHandlerEvent event) {
        IPayloadRegistrar registrar = event.registrar(Utils.MOD_ID).versioned("1.0.0");

        registrar.play(ClientboundUpdateRecipesMessage.ID, ClientboundUpdateRecipesMessage::decode, handler -> {
            handler.client(ClientboundUpdateRecipesMessage::handle);
        });
    }

    private void registerContent(IEventBus modBus, Content content) {
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, entity, side) -> {
                    return CommonMain.getItemAccess(level, pos, state, entity).map(access -> {
                        return (IItemHandlerModifiable) access.get();
                    }).orElse(null);
                },
                content.getBlocks().stream().map(NamedValue::getValue).toArray(OpenableBlock[]::new)
            );
        });

        modBus.addListener((RegisterEvent event) -> {
            event.register(Registries.STAT_TYPE, helper -> {
                content.getStats().forEach(it -> Registry.register(BuiltInRegistries.CUSTOM_STAT, it, it));
            });

            event.register(Registries.BLOCK, helper -> {
                CommonMain.iterateNamedList(content.getBlocks(), helper::register);
            });

            event.register(Registries.ITEM, helper -> {
                CommonMain.iterateNamedList(content.getItems(), helper::register);
            });

            event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
                ForgeMain.registerBlockEntity(helper, content.getChestBlockEntityType());
                ForgeMain.registerBlockEntity(helper, content.getOldChestBlockEntityType());
                ForgeMain.registerBlockEntity(helper, content.getBarrelBlockEntityType());
                ForgeMain.registerBlockEntity(helper, content.getMiniChestBlockEntityType());
            });

            event.register(Registries.ENTITY_TYPE, helper -> {
                CommonMain.iterateNamedList(content.getEntityTypes(), helper::register);
            });

            event.register(Registries.CREATIVE_MODE_TAB, helper -> {
                helper.register(Utils.id("tab"), CreativeModeTab
                        .builder()
                        .icon(() -> BuiltInRegistries.ITEM.get(Utils.id("netherite_chest")).getDefaultInstance())
                        .displayItems((itemDisplayParameters, output) -> {
                            CommonMain.generateDisplayItems(itemDisplayParameters, stack -> {
                                output.accept(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                            });
                        })
                        .title(Component.translatable("itemGroup.expandedstorage.tab"))
                        .build()
                );
            });
        });

        // Hopefully if another mod replaces this supplier we'll capture theirs here.
        Supplier<BiMap<Block, Block>> originalWaxablesMap = HoneycombItem.WAXABLES;
        HoneycombItem.WAXABLES = Suppliers.memoize(() -> {
            return ImmutableBiMap.<Block, Block>builder()
                                 // Hopefully the original / modded map is okay to query here.
                                 .putAll(originalWaxablesMap.get())
                                 .putAll(CopperBlockHelper.dewaxing().inverse())
                                 .build();
        });

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ForgeClient.initialize(modBus, content);
        }
    }

    private static <T extends BlockEntity> void registerBlockEntity(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper, NamedValue<BlockEntityType<T>> blockEntityType) {
        helper.register(blockEntityType.getName(), blockEntityType.getValue());
    }
}
