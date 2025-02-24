package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import us.ri0.deli.Addon;
import us.ri0.deli.TextUtils;
import us.ri0.deli.chunkutils.BlockUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;

import java.util.concurrent.ConcurrentHashMap;

public class MinecartToucher extends Module {
    public MinecartToucher() {
        super(Addon.CATEGORY, "cart-toucher", "Detect when a minecart chest has been touched by a player");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Rendering");

    private final Setting<Boolean> chatNotification = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-notification")
        .description("Notifies you in chat when a minecart chest has been touched")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Color of box around entity")
        .defaultValue(new SettingColor(0, 255, 0))
        .build()
    );

    private final Setting<SettingColor> boxColor = sgRender.add(new ColorSetting.Builder()
        .name("box-color")
        .description("Color of entity's bounding box")
        .defaultValue(new SettingColor(0, 255, 0, 30))
        .build()
    );

    private final Setting<Boolean> tracer = sgRender.add(new BoolSetting.Builder()
        .name("tracer")
        .description("Draws a line to the minecart")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("Color of the tracer line")
        .defaultValue(new SettingColor(0, 255, 0))
        .visible(tracer::get)
        .build()
    );
    private final Esp esp = new Esp();
    private final EspOptions opts = new EspOptions();
    private final ConcurrentHashMap<Integer, Boolean> seen = new ConcurrentHashMap<>();

    @EventHandler
    public void onRender3d(Render3DEvent event) {
        mc.world.getEntities().forEach(entity -> {
            if (!(entity instanceof ChestMinecartEntity)) return;

            Vec3d pos = entity.getPos();
            if(Math.abs(pos.x % 1) != 0.5 || Math.abs(pos.z % 1) != 0.5) {
                // Check for fluid collision
                if(BlockUtils.isFluid(entity.getBlockPos().east())) return;
                if(BlockUtils.isFluid(entity.getBlockPos().west())) return;
                if(BlockUtils.isFluid(entity.getBlockPos().north())) return;
                if(BlockUtils.isFluid(entity.getBlockPos().south())) return;

                // Also check diagonals
                if(BlockUtils.isFluid(entity.getBlockPos().north().east())) return;
                if(BlockUtils.isFluid(entity.getBlockPos().north().west())) return;
                if(BlockUtils.isFluid(entity.getBlockPos().south().east())) return;
                if(BlockUtils.isFluid(entity.getBlockPos().south().west())) return;


                var bPos = entity.getBlockPos();
                if(esp.isNew(bPos)) {
                    esp.Block(bPos, opts);
                    if (chatNotification.get()) {
                        var btn = TextUtils.circleCommandLink(bPos.toCenterPos());
                        var coords = TextUtils.coords(bPos.toCenterPos());
                        Text msg = Text.literal("Minecart was touched at ").append(coords).append(btn);
                        ChatUtils.sendMsg("CartToucher",  msg);
                    }
                }
            }
        });
        esp.onRender3D(event);
    }

    @Override
    public void onActivate() {
        opts.lineColor = lineColor;
        opts.sideColor = boxColor;
        opts.tracer = tracer;
        opts.tracerColor = tracerColor;
    }

    @Override
    public void onDeactivate() {
        esp.clear();
    }


}
