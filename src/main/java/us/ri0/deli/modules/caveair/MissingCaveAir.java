package us.ri0.deli.modules.caveair;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.world.chunk.WorldChunk;
import us.ri0.deli.Addon;
import us.ri0.deli.chunkutils.ChunkUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;

public class MissingCaveAir extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDungeons = settings.createGroup("Dungeons Scans");
    private final SettingGroup sgMineshaft = settings.createGroup("Mineshaft Scans");
    private final SettingGroup sgDebug = settings.createGroup("Debugging");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("Render Distance (chunks)")
        .description("Maximum distance to render the ESP box for any discovers")
        .defaultValue(15)
        .min(1)
        .sliderMax(64)
        .build()
    );

    private final Setting<Boolean> chatNotifications = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-notifications")
        .description("Notifies you in chat with coords when a discovery is made")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> scanMinecarts = sgMineshaft.add(new BoolSetting.Builder()
        .name("scan-minecarts")
        .description("Scan area around minecarts for missing cave_air")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> minecartLineColor = sgMineshaft.add(new ColorSetting.Builder()
        .name("minecart-line-color")
        .description("Color of box around entity")
        .defaultValue(new SettingColor(255, 200, 200))
        .visible(scanMinecarts::get)
        .build()
    );

    private final Setting<SettingColor> minecartBoxColor = sgMineshaft.add(new ColorSetting.Builder()
        .name("minecart-box-color")
        .description("Color of entity's bounding box")
        .defaultValue(new SettingColor(255, 200, 200, 20))
        .visible(scanMinecarts::get)
        .build()
    );

    private final Setting<Boolean> minecartTracer = sgMineshaft.add(new BoolSetting.Builder()
        .name("minecart-tracer")
        .description("Draws a line to the entity")
        .defaultValue(false)
        .visible(scanMinecarts::get)
        .build()
    );

    private final Setting<SettingColor> minecartTracerColor = sgMineshaft.add(new ColorSetting.Builder()
        .name("minecart-tracer-color")
        .description("Color of the tracer line")
        .defaultValue(new SettingColor(255, 200, 200))
        .visible(scanMinecarts::get)
        .build()
    );



    private final Setting<Boolean> scanDungeons = sgDungeons.add(new BoolSetting.Builder()
        .name("scan-dungeons")
        .description("Scan area around dungeon spawners for missing cave_air")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> dungeonLineColor = sgDungeons.add(new ColorSetting.Builder()
        .name("dungeon-line-color")
        .description("Color of box around entity")
        .defaultValue(new SettingColor(0, 200, 150))
        .visible(scanDungeons::get)
        .build()
    );

    private final Setting<SettingColor> dungeonBoxColor = sgDungeons.add(new ColorSetting.Builder()
        .name("dungeon-box-color")
        .description("Color of entity's bounding box")
        .defaultValue(new SettingColor(0, 200, 150, 40))
        .visible(scanDungeons::get)
        .build()
    );

    private final Setting<Boolean> dungeonTracer = sgDungeons.add(new BoolSetting.Builder()
        .name("dungeon-tracer")
        .description("Draws a line to the entity")
        .defaultValue(false)
        .visible(scanDungeons::get)
        .build()
    );

    private final Setting<SettingColor> dungeonTracerColor = sgDungeons.add(new ColorSetting.Builder()
        .name("dungeon-tracer-color")
        .description("Color of the tracer line")
        .defaultValue(new SettingColor(0, 200, 150))
        .visible(scanDungeons::get)
        .build()
    );

    private final Setting<Boolean> debug = sgDebug.add(new BoolSetting.Builder()
        .name("debug")
        .description("Debugging information")
        .defaultValue(false)
        .build()
    );

    private final Esp esp = new Esp();
    private DungeonScanner dungeonScanner;
    private MineshaftCartScanner cartScanner;


    public MissingCaveAir() {
        super(Addon.CATEGORY, "CaveAir", "Highlight air that should be cave_air");
    }

    @EventHandler
    public void onRender3d(Render3DEvent event) {
        esp.onRender3D(event);

        for (var entity : mc.world.getEntities()) {
            if (entity instanceof ChestMinecartEntity cart) {
                MeteorExecutor.execute(() -> {
                    cartScanner.scanEntity(cart);
                });
            }
        }

    }

    @EventHandler
    public void onChunkData(ChunkDataEvent event) {
        if (debug.get()) {
            //MeteorExecutor.execute(() -> DebugScans.touchPoints(esp, event.chunk()));
            MeteorExecutor.execute(() -> DebugScans.highlightCaveAir(esp, event.chunk()));
        }

        if(scanDungeons.get()) {
            if(!ChunkUtils.hasAny(event.chunk(), Blocks.CAVE_AIR)) return;
            dungeonScanner.scanChunk(event.chunk());
        }
    }


        @Override
    public void onActivate() {
        var dungeonEspOpts = new EspOptions();
        dungeonEspOpts.renderDistance = range;
        dungeonEspOpts.sideColor = dungeonBoxColor;
        dungeonEspOpts.lineColor = dungeonLineColor;
        dungeonEspOpts.tracer = dungeonTracer;
        dungeonEspOpts.tracerColor = dungeonTracerColor;
        dungeonScanner = new DungeonScanner(esp, dungeonEspOpts);

        var minecartOpts = new EspOptions();
        minecartOpts.renderDistance = range;
        minecartOpts.sideColor = minecartBoxColor;
        minecartOpts.lineColor = minecartLineColor;
        minecartOpts.tracer = minecartTracer;
        minecartOpts.tracerColor = minecartTracerColor;
        cartScanner = new MineshaftCartScanner(esp, minecartOpts);

        for(var c : Utils.chunks(true)) {
            onChunkData(new ChunkDataEvent((WorldChunk) c));
        }
    }

    @Override
    public void onDeactivate() {
        esp.clear();
        dungeonScanner.clear();
    }
}
