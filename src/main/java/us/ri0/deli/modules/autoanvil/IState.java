package us.ri0.deli.modules.autoanvil;

public interface IState {
    IState next(Context ctx);
}
