package us.ri0.deli.modules.autoanvil;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class StateCloseScreen implements IState{
    private IState next;
    public StateCloseScreen(IState next) {
        this.next = next;
    }

    @Override
    public IState next(Context ctx) {
        if(mc.currentScreen != null) {
            mc.currentScreen.close();
            return new StateWaitForScreen(null, next);
        }
        return next;
    }
}
