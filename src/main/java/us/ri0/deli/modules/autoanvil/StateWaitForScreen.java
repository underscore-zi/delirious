package us.ri0.deli.modules.autoanvil;

import net.minecraft.client.gui.screen.Screen;

import static meteordevelopment.meteorclient.MeteorClient.mc;

class StateWaitForScreen implements IState {
    private final Class<? extends Screen> waitFor;
    private final IState doNext;
    public StateWaitForScreen(Class<? extends Screen> screen, IState next) {
        this.waitFor = screen;
        this.doNext = next;
    }

    @Override public IState next(Context ctx) {
        if(waitFor == null) {
            if(mc.currentScreen == null) {
                return doNext;
            } else {
                return this;
            }
        } else if(waitFor.isInstance(mc.currentScreen)) {
            return doNext;
        } else {
            return this;
        }
    }
}
