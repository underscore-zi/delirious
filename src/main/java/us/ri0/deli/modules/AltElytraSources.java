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
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import us.ri0.deli.Addon;
import us.ri0.deli.TextUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;

import java.util.HashSet;

public class AltElytraSources extends Module {
    private final SettingGroup sgRender = settings.createGroup("Rendering");
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Color of box around entity")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    private final Setting<SettingColor> boxColor = sgRender.add(new ColorSetting.Builder()
        .name("box-color")
        .description("Color of entity's bounding box")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final Setting<Boolean> tracer = sgRender.add(new BoolSetting.Builder()
        .name("tracer")
        .description("Draws a line to the entity")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("Color of the tracer line")
        .defaultValue(new SettingColor(255, 0, 0))
        .visible(tracer::get)
        .build()
    );

    public AltElytraSources() {
        super(Addon.CATEGORY, "alt-elytra", "Highlight alternative elytra sources (Piglins who picked them up, and dropped items)");
    }

    private Esp esp = new Esp();
    private EspOptions opts = new EspOptions();
    @EventHandler
    public void onRender3d(Render3DEvent event) {
        for(Entity entity : mc.world.getEntities()) {
            if(entity instanceof ItemEntity item) {
                if(item.getStack().getItem().toString().contains("elytra")) {
                    onDiscovery(item, "dropped elytra");
                }
            } else if (entity instanceof MobEntity mob) {
                mob.getArmorItems().forEach((item) -> {
                    if (item.getItem().toString().equals("minecraft:elytra")) {
                        String mobName = mob.getName().getString();
                        onDiscovery(mob, "flying " + mobName);
                    }
                });
            }
        }

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
        discoveredEntities.clear();
    }

    private HashSet<Integer> discoveredEntities = new HashSet<>();
    public void onDiscovery(Entity entity, String name) {
        if(discoveredEntities.contains(entity.getId())) return;
        discoveredEntities.add(entity.getId());

        esp.Entity(entity, opts);

        var coords = TextUtils.coords(entity.getPos());
        MutableText text = Text.literal("Found a ")
            .append(name).append(" at ").append(coords)
            .append(TextUtils.circleCommandLink(entity.getPos()));
        ChatUtils.sendMsg("Alt Elytra", text);


    }


}
