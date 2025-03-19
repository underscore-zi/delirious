package us.ri0.deli.modules.autoanvil;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class StateMoveFromSlot implements IState {
    private final IState next;
    private final List<Integer> slots;
    private final BlockPos pos;
    private final boolean updateInventory;

    public StateMoveFromSlot(BlockPos pos, List<Integer> slots, boolean updateInventory, IState next) {
        this.next = next;
        this.slots = slots;
        this.pos = pos;
        this.updateInventory = updateInventory;
    }

    @Override
    public IState next(Context ctx) {
        if (slots.isEmpty()) return next;

        if(mc.currentScreen == null) {
            return new StateOpenContainer(pos, this);
        }

        if(mc.currentScreen instanceof GenericContainerScreen screen) {
            var handler = screen.getScreenHandler();
            var storageSlots = handler.slots;
            var slotId = slots.get(0);
            slots.remove(0);
            mc.interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.QUICK_MOVE, mc.player);
            removeFromInventoryCache(ctx, slotId);
        } else if (mc.currentScreen instanceof ShulkerBoxScreen screen) {
            var handler = screen.getScreenHandler();
            var storageSlots = handler.slots;
            var slotId = slots.get(0);
            slots.remove(0);
            mc.interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.QUICK_MOVE, mc.player);
            removeFromInventoryCache(ctx, slotId);
        }

        return this;
    }

    public void removeFromInventoryCache(Context ctx, int slotId) {
        if(!updateInventory) return;
        ctx.inventories.get(pos).set(slotId, ItemStack.EMPTY);
    }
}
