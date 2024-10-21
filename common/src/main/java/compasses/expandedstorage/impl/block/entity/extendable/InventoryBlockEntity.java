package compasses.expandedstorage.impl.block.entity.extendable;

import compasses.expandedstorage.impl.block.strategies.Observable;
import compasses.expandedstorage.impl.inventory.WrappedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public abstract class InventoryBlockEntity extends OpenableBlockEntity implements WrappedInventory {
    private final NonNullList<ItemStack> items;
    private Observable observable;
    private final WorldlyContainer inventory = new WorldlyContainer() {
        private int[] availableSlots;

        @Override
        public int @NotNull [] getSlotsForFace(Direction side) {
            if (availableSlots == null) {
                availableSlots = IntStream.range(0, this.getContainerSize()).toArray();
            }
            return availableSlots;
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            return true;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            return true;
        }

        @Override
        public int getContainerSize() {
            return items.size();
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : items) {
                if (stack.isEmpty()) continue;
                return false;
            }
            return true;
        }

        @NotNull
        @Override
        public ItemStack getItem(int slot) {
            return items.get(slot);
        }

        @NotNull
        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
            if (!stack.isEmpty()) this.setChanged();
            return stack;
        }

        @NotNull
        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ContainerHelper.takeItem(items, slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
            items.set(slot, stack);
            this.setChanged();
        }

        @Override
        public void setChanged() {
            InventoryBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return InventoryBlockEntity.this.isValidAndPlayerInRange(player);
        }

        @Override
        public void clearContent() {
            items.clear();
        }

        @Override
        public void startOpen(Player player) {
            if (player.isSpectator() || observable == null || InventoryBlockEntity.this.isRemoved()) return;
            observable.playerStartViewing(player);
        }

        @Override
        public void stopOpen(Player player) {
            if (player.isSpectator() || observable == null || InventoryBlockEntity.this.isRemoved()) return;
            observable.playerStopViewing(player);
        }
    };

    public InventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ResourceLocation blockId, Component defaultName, int inventorySize) {
        super(type, pos, state, blockId, defaultName);
        items = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    }

    @Override
    public final WorldlyContainer getInventory() {
        return inventory;
    }

    @Override
    public final NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        ContainerHelper.loadAllItems(tag, items, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        ContainerHelper.saveAllItems(tag, items, provider);
    }

    protected void setObservable(Observable observable) {
        if (this.observable == null) this.observable = observable;
    }
}

