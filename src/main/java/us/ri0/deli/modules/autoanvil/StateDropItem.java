package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.screen.slot.SlotActionType;

import java.util.concurrent.atomic.AtomicBoolean;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class StateDropItem implements IState {
    private IState next;
    private int slot;
    AtomicBoolean completed = new AtomicBoolean(false);

    public StateDropItem(int slot, IState next) {
        this.next = next;
        this.slot = slot;
    }

    @Override
    public IState next(Context ctx) {
        if(mc.currentScreen == null) {
            throw new RuntimeException("No screen open, unable to drop from slot");
        }

        var dir = mc.player.getHorizontalFacing();
        var tPos = mc.player.getBlockPos().down(2);//.add(dir.getOffsetX(), -1, dir.getOffsetZ());

        Rotations.rotate(270, Rotations.getYaw(tPos), 100, true, () -> {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.THROW, mc.player);
            completed.set(true);
        });

        if(completed.get()) {
            return next;
        }
        return this;
    }

}
