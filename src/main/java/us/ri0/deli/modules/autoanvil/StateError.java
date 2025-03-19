package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.utils.player.ChatUtils;

class StateError implements IState {
    private final String message;
    public StateError(String message) {
        this.message = message;
    }
    @Override
    public IState next(Context ctx) {
        if(!message.isEmpty()) {
            ChatUtils.errorPrefix(AutoAnvil.PREFIX, message);
        }
        return null;
    }
}
