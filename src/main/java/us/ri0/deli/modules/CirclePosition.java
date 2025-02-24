package us.ri0.deli.modules;

import baritone.api.BaritoneAPI;
import baritone.api.process.IElytraProcess;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import us.ri0.deli.Addon;

public class CirclePosition extends Module {
    public CirclePosition() {
        super(Addon.CATEGORY, "circle-pos", "Just circle the given coordinate");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> xPos = sgGeneral.add(new IntSetting.Builder()
        .name("x-pos")
        .description("X coordinate to circle around")
        .defaultValue(0)
        .sliderMax(30000000)
        .sliderMin(-30000000)
        .build()
    );

    private final Setting<Integer> yLevel = sgGeneral.add(new IntSetting.Builder()
        .name("y-level")
        .description("Desired flight level, it will still navigate around terrain as necessary.")
        .defaultValue(180)
        .sliderMax(320)
        .sliderMin(65)
        .build()
    );

    public final Setting<Integer> zPos = sgGeneral.add(new IntSetting.Builder()
        .name("z-pos")
        .description("X coordinate to circle around")
        .defaultValue(0)
        .sliderMax(30000000)
        .sliderMin(-30000000)
        .build()
    );
    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("Radius")
        .defaultValue(75)
        .sliderMax(1000)
        .sliderMin(50)
        .build()
    );

    private IElytraProcess efly;
    private int sideDistance = 0;
    private final int[][] multipliers = {
        {0,1}, {1,0}, {0,-1}, {-1,0}
    };
    private int multiIndex = 0;
    private BlockPos lastDest = null;


    private void reset() {
        multiIndex = 0;
        efly = BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess();
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if(efly == null || !efly.isActive()) {
            this.toggle();
            return;
        }

        var pos = mc.player.getBlockPos();
        var dest = efly.currentDestination();

        var deltaX = Math.abs(pos.getX() - dest.getX());
        var deltaY = Math.abs(pos.getZ() - dest.getZ());

        if(deltaX < 50 && deltaY < 50) {
            lastDest = getNextDestination();
            efly.pathTo(lastDest);
        }
    }

    private BlockPos getNextDestination() {
        var multi = multipliers[multiIndex];
        multiIndex = (multiIndex + 1) % multipliers.length;

        var out = new BlockPos(
            xPos.get() + (multi[0] * radius.get()),
            yLevel.get(),
            zPos.get() + (multi[1] * radius.get())
        );
        return out;
    }

    @Override
    public void onDeactivate() {
        if(efly == null || !efly.isActive()) return;
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("stop");
    }
    @Override
    public void onActivate() {
        if(PlayerUtils.getDimension() == Dimension.Nether) {
            ChatUtils.errorPrefix("circle-pos", "This module is not supported in the nether");
            this.toggle();
            return;
        }

        this.reset();

        lastDest = getNextDestination();
        efly.pathTo(lastDest);
    }

}
