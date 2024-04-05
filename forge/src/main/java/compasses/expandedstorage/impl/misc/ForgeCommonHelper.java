package compasses.expandedstorage.impl.misc;

import compasses.expandedstorage.impl.recipe.BlockConversionRecipe;
import compasses.expandedstorage.impl.recipe.EntityConversionRecipe;
import compasses.expandedstorage.impl.inventory.handler.AbstractHandler;
import compasses.expandedstorage.impl.registration.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForgeCommonHelper implements CommonPlatformHelper {
    private final MenuType<AbstractHandler> menuType;

    {
        menuType = new MenuType<>((IContainerFactory<AbstractHandler>) AbstractHandler::createClientMenu, FeatureFlags.VANILLA_SET);
    }

    @Override
    public MenuType<AbstractHandler> getScreenHandlerType() {
        return menuType;
    }

    @Override
    public void openScreenHandler(ServerPlayer player, Container inventory,Component title, ResourceLocation forcedScreenType) {
        player.openMenu(new SimpleMenuProvider((syncId, playerInventory, _p) -> {
            return new AbstractHandler(syncId, inventory, playerInventory, forcedScreenType);
        }, title), buffer -> {
            buffer.writeInt(inventory.getContainerSize());
            buffer.writeNullable(forcedScreenType, FriendlyByteBuf::writeResourceLocation);
        });
    }

    @Override
    public void sendConversionRecipesToClient(@Nullable ServerPlayer target, List<BlockConversionRecipe<?>> blockRecipes, List<EntityConversionRecipe<?>> entityRecipes) {
        if (target == null) {
            // Should be valid to send updates here as remote present check has been done on join.
            PacketDistributor.ALL.noArg().send(new ClientboundUpdateRecipesMessage(blockRecipes, entityRecipes));
        } else {
            PacketDistributor.PLAYER.with(target).send(new ClientboundUpdateRecipesMessage(blockRecipes, entityRecipes));
        }
    }

    @Override
    public boolean canDestroyBamboo(ItemStack stack) {
        return stack.canPerformAction(ToolActions.SWORD_DIG);
    }

    @Override
    public boolean isWoodenChest(BlockState state) {
        return state.is(ModBlocks.OLD_WOOD_CHEST) || state.is(Tags.Blocks.CHESTS_WOODEN);
    }
}
