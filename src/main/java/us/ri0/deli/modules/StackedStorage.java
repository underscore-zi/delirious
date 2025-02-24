package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import us.ri0.deli.Addon;
import us.ri0.deli.TextUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;

import java.util.HashMap;

public class StackedStorage extends Module {
    public StackedStorage() {
        super(Addon.CATEGORY, "stacked-storage", "Detect when multiple storage minecarts are stacked in the same position.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Rendering");

    private final Setting<Boolean> chatNotification = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-notification")
        .description("Notifies you in chat when a minecart chest has been touched")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> chatCoords = sgGeneral.add(new BoolSetting.Builder()
        .name("include-coords-in-chat")
        .description("Include coordinates in chat notification")
        .defaultValue(true)
        .visible(chatNotification::get)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Color of box around entity")
        .defaultValue(new SettingColor(255, 113, 71))
        .build()
    );

    private final Setting<SettingColor> boxColor = sgRender.add(new ColorSetting.Builder()
        .name("box-color")
        .description("Color of entity's bounding box")
        .defaultValue(new SettingColor(255, 113, 71, 30))
        .build()
    );

    private final Setting<Boolean> tracer = sgRender.add(new BoolSetting.Builder()
        .name("tracer")
        .description("Draws a line to the entities")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("Color of the tracer line")
        .defaultValue(new SettingColor(255, 113, 71))
        .visible(tracer::get)
        .build()
    );

    private Esp esp = new Esp();
    private EspOptions opts = new EspOptions();
    private boolean newEntities = false;

    @EventHandler
    public void onRender3d(Render3DEvent event) {
        esp.onRender3D(event);

        if(newEntities) {
            newEntities = false;
            MeteorExecutor.execute(this::scan);
        }
    }

    /**
     * Grabs all loaded entities and scans for any "interesting" entities that exist within the same block position
     */
    private void scan() {
        HashMap<BlockPos, Integer> counts = new HashMap<>();
        for (var entity : mc.world.getEntities()) {
            if(isInterestingEntity(entity)) {
                var pos = entity.getBlockPos();
                if(counts.containsKey(pos)) {
                    counts.put(pos, counts.get(pos) + 1);
                } else {
                    counts.put(pos, 1);
                }
            }
        }

        counts.entrySet().removeIf(entry -> entry.getValue() <= 1);

        for (var entry : counts.entrySet()) {
            var pos = entry.getKey();
            var count = entry.getValue();

            if(!esp.isNew(pos)) continue;
            esp.Block(pos, opts);

            if(chatNotification.get()) {
                var message = Text.literal(String.format("Detected %d stacked storage entities", count));
                if(chatCoords.get()) {
                    var coords = TextUtils.coords(pos.toCenterPos());
                    message.append(" at ").append(coords);
                }
                message.append(TextUtils.circleCommandLink(pos.toCenterPos()));
                ChatUtils.sendMsg("StackedStorage", message);
            }
        }
    }

    /**
     * Determine if the new entity is one that might be stacked that we are interested in tracking
     * @param entity
     */
    private boolean isInterestingEntity(Entity entity) {
        return entity instanceof StorageMinecartEntity || entity instanceof FurnaceMinecartEntity;
    }

    @EventHandler
    public void onEntityAdded(EntityAddedEvent ev) {
        /*
         * I was initially going to do the scan from this event which would probably be the most performant option.
         * However, from here I cannot determine when new entities are done being added, so I cannot determine the final
         * count of overlapping entities which while not necessary is nice to have. The solution is t run teh full scan
         * in the render event, but use this event to trigger it so we don't consume too many render cycles.
         */

        if(isInterestingEntity(ev.entity)) {
            newEntities = true;
        }
    }

    @Override
    public void onActivate() {
        opts.lineColor = lineColor;
        opts.sideColor = boxColor;
        opts.tracer = tracer;
        opts.tracerColor = tracerColor;

        newEntities = true;
    }

    @Override
    public void onDeactivate() {
        esp.clear();
    }
}
