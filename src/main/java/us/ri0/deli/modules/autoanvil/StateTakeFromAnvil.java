package us.ri0.deli.modules.autoanvil;

import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.concurrent.atomic.AtomicReference;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class StateTakeFromAnvil implements IState {
    AtomicReference<ItemStack> result;
    IState next;

    public StateTakeFromAnvil(AtomicReference<ItemStack> result, IState next) {
        this.result = result;
        this.next = next;
    }

    @Override
    public IState next(Context ctx) {
        if (!(mc.currentScreen instanceof AnvilScreen)) {
            // Just pause until we are on an anvil screen, its probably broken if we got in here anyhow
            ctx.addDelay(10);
            return this;
        }
        var handler = ((AnvilScreen) mc.currentScreen).getScreenHandler();

        // Error path, we don't have enough XP to enchant
        if(handler.getLevelCost() > mc.player.experienceLevel) {
            if(next instanceof StateEnchant estep) {
                estep.restartLastStep();
            } else {
                throw new RuntimeException("Expected next state to be StateEnchant");
            }

            if(mc.currentScreen!=null) mc.currentScreen.close();
            return new StateWaitForScreen(null,
                new StateGainXP(handler.getLevelCost(),
                    new StateOpenAnvil(next)));
        }

        //Happy path, should enchant successfully
        var newItem = handler.getSlot(2).getStack();
        result.set(newItem);

        mc.interactionManager.clickSlot(handler.syncId, 2, 0, SlotActionType.QUICK_MOVE, mc.player);
        return next;
    }
}
