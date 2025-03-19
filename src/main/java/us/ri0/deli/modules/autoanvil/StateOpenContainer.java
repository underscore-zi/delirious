package us.ri0.deli.modules.autoanvil;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class StateOpenContainer implements IState {
    private final int PERSONAL_INVENTORY_SIZE = 36;
    private final IState next;
    private final BlockPos pos;
    private final boolean canFail;
    private boolean interacted = false;
    private int runsSinceInteration = 0;

    public StateOpenContainer(BlockPos pos, IState next) {
        this.pos = pos;
        this.next = next;
        this.canFail = false;
    }

    public StateOpenContainer(BlockPos pos, boolean canFail, IState next) {
        this.pos = pos;
        this.next = next;
        this.canFail = canFail;
    }

    @Override
    public IState next(Context ctx) {
        if (mc.currentScreen == null) {
            if(interacted) {
                runsSinceInteration++;
                if(runsSinceInteration > 20) {
                    return canFail?next:new StateError("Failed to open container.");
                }
                return this;
            }

            BlockHitResult hitResult = new BlockHitResult(pos.toCenterPos(), Direction.UP, pos, false);
            if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult) == ActionResult.SUCCESS) {
                interacted = true;
                runsSinceInteration = 0;
                return this;
            }

            return new StateError("Failed to open container.");
        } else if(mc.currentScreen instanceof GenericContainerScreen screen)  {
            updateInventory(ctx, pos, screen.getScreenHandler().slots);
            return next;
        } else if (mc.currentScreen instanceof ShulkerBoxScreen screen) {
            updateInventory(ctx, pos, screen.getScreenHandler().slots);
            return next;
        }

        return this;

    }

    private void updateInventory(Context ctx, BlockPos pos, DefaultedList<Slot> slots) {
        List<ItemStack> inventory = new ArrayList<>(slots.size() - PERSONAL_INVENTORY_SIZE);
        for(int i = 0; i < slots.size() - PERSONAL_INVENTORY_SIZE; i++) {
            inventory.add(slots.get(i).getStack());
        }
        ctx.setInventory(pos, inventory);
    }
}
