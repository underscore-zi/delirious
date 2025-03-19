package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class StateMoveToAnvilSlot implements IState {
    private int left;
    private int right;
    private IState next;


    public StateMoveToAnvilSlot(int left, int right, IState next) {
        this.left = left;
        this.right = right;
        this.next = next;
    }

    @Override
    public IState next(Context ctx) {
        if(!(mc.currentScreen instanceof AnvilScreen)) {
            // Just pause until we are on an anvil screen, its probably broken if we got in here anyhow
            ctx.addDelay(10);
            return this;
        }

        var handler = ((AnvilScreen) mc.currentScreen).getScreenHandler();
        InvUtils.shiftClick().slot(left);
        InvUtils.shiftClick().slot(right);

        return next;
    }
}
