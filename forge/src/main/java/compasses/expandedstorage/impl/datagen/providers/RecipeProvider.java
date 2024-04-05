package compasses.expandedstorage.impl.datagen.providers;

import compasses.expandedstorage.impl.datagen.content.ForgeTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.Tags;

public final class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    public RecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        RecipeHelper recipeHelper = new RecipeHelper(
                BuiltInRegistries.ITEM::getKey,
                Tags.Items.INGOTS_COPPER, Tags.Items.NUGGETS_IRON, Tags.Items.INGOTS_IRON, Tags.Items.INGOTS_GOLD, Tags.Items.GEMS_DIAMOND, Tags.Items.OBSIDIAN, Tags.Items.INGOTS_NETHERITE,
                Tags.Items.CHESTS_WOODEN, Tags.Items.BARRELS_WOODEN,
                Tags.Items.GLASS, Tags.Items.DYES_RED, Tags.Items.DYES_WHITE, ForgeTags.Items.BAMBOO
        );
        recipeHelper.registerRecipes(output);
    }

    @Override
    public String getName() {
        return "Expanded Storage - Recipes";
    }
}
