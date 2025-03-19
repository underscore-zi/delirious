package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class StateGainXP implements IState {
    private final IState next;
    private final int targetXPLevel;

    public StateGainXP(int targetXPLevel, IState next) {
        this.next = next;
        this.targetXPLevel = targetXPLevel;
    }

    @Override
    public IState next(Context ctx) {
        if(ctx.useKillAuraForXP() && !Modules.get().get(KillAura.class).isActive()) {
            Modules.get().get(KillAura.class).toggle();
        }

        if(mc.player.experienceLevel >= targetXPLevel) {
            if(ctx.useKillAuraForXP() && Modules.get().get(KillAura.class).isActive()) {
                Modules.get().get(KillAura.class).toggle();
            }

            return next;
        }

        return this;
    }
}
