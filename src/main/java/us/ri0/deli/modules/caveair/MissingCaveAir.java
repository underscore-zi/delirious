package us.ri0.deli.modules.caveair;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import us.ri0.deli.Addon;
import us.ri0.deli.chunkutils.BlockUtils;
import us.ri0.deli.chunkutils.ChunkUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;
import us.ri0.deli.esp.MockSetting;

import java.util.HashSet;

public class MissingCaveAir extends Module {
    private final String chatPrefix = "CaveAir";
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDungeons = settings.createGroup("Dungeons Scans");
    private final SettingGroup sgMineshaft = settings.createGroup("Mineshaft Scans");
    private final SettingGroup sgTouch = settings.createGroup("Touch Scans");
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

    private final Setting<Boolean> showAllTouches = sgTouch.add(new BoolSetting.Builder()
        .name("show-all-touches")
        .description("Show all potential player created air pockets in cave_air. Basically this shows all air blocks that touch cave_air blocks")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> showCaveAir = sgTouch.add(new BoolSetting.Builder()
        .name("show-cave-air")
        .description("Add box around cave_air")
        .defaultValue(false)
        .visible(showAllTouches::get)
        .build()
    );

    private final Setting<SettingColor> touchCaveAirLineColor = sgTouch.add(new ColorSetting.Builder()
        .name("touch-cave-air-color")
        .description("Line color of touched cave_air")
        .defaultValue(new SettingColor(0, 200, 0))
        .visible(showAllTouches::get)
        .build()
    );

    private final Setting<SettingColor> touchCaveAirSideColor = sgTouch.add(new ColorSetting.Builder()
        .name("touch-cave-air-color")
        .description("Side color of touched cave_air")
        .defaultValue(new SettingColor(0, 200, 0, 10))
        .visible(showAllTouches::get)
        .build()
    );

    private final Setting<SettingColor> airBlockLineColor = sgTouch.add(new ColorSetting.Builder()
        .name("touch-air-line-color")
        .description("Line color of the lines around the air block that triggered the scan")
        .defaultValue(new SettingColor(0, 0, 200, 150))
        .visible(showAllTouches::get)
        .build()
    );

    private final Setting<SettingColor> airBlockSideColor = sgTouch.add(new ColorSetting.Builder()
        .name("touch-air-color")
        .description("Side color of the sides around the air block that triggered the scan")
        .defaultValue(new SettingColor(0, 0, 200, 50))
        .visible(showAllTouches::get)
        .build()
    );

    private final Setting<Boolean> airBlockTracer = sgTouch.add(new BoolSetting.Builder()
        .name("touch-air-tracer")
        .description("Draws a line to the air block that triggered the scan")
        .defaultValue(false)
        .visible(showAllTouches::get)
        .build()
    );

    private final Setting<SettingColor> airBlockTracerColor = sgTouch.add(new ColorSetting.Builder()
        .name("touch-air-tracer-color")
        .description("Color of the tracer line")
        .defaultValue(new SettingColor(0, 0, 200))
        .visible(showAllTouches::get)
        .build()
    );

    private final Setting<Boolean> ignoreFullySurrounded = sgTouch.add(new BoolSetting.Builder()
        .name("ignore-fully-surrounded")
        .description("Ignore blocks that are fully surrounded by cave air as its not a place a player could have placed a block.")
        .defaultValue(true)
        .visible(showAllTouches::get)
        .build()
    );

    private final Setting<Integer> touchThreshold = sgTouch.add(new IntSetting.Builder()
        .name("touch-threshold")
        .description("The minimum number of cave_air blocks that the air block must be touching to trigger")
        .defaultValue(4)
        .min(1)
        .sliderMax(6)
        .visible(showAllTouches::get)
        .build()
    );


    private final Esp esp = new Esp();
    private final EspOptions dungeonOpts = new EspOptions();
    private final EspOptions minecartOpts = new EspOptions();

    private final EspOptions touchAirOpts = new EspOptions();
    private final EspOptions touchCaveAirOpts = new EspOptions();


    private final HashSet<BlockPos> scannedCarts = new HashSet<>();

    private DungeonScanner dungeonScanner;
    private MineshaftCartScanner cartScanner;


    public MissingCaveAir() {
        super(Addon.CATEGORY, "CaveAir", "Highlight air that should be cave_air");
    }

    @EventHandler
    public void onRender3d(Render3DEvent event) {
        esp.onRender3D(event);

        if(scanMinecarts.get()) {
            for (var entity : mc.world.getEntities()) {
                if (entity instanceof ChestMinecartEntity cart) {
                    synchronized (scannedCarts) {
                        if (scannedCarts.contains(cart.getBlockPos())) continue;
                        scannedCarts.add(cart.getBlockPos());
                    }

                    MeteorExecutor.execute(() -> {
                        cartScanner.scanEntity(cart);
                    });
                }
            }

            synchronized (scannedCarts) {
                scannedCarts.removeIf(pos -> !BlockUtils.isLoaded(pos));
            }
        }
    }

    @EventHandler
    public void onChunkData(ChunkDataEvent event) {
        if (debug.get()) {
            MeteorExecutor.execute(() -> DebugScans.highlightCaveAir(esp, event.chunk()));
        }

        if(scanDungeons.get()) {
            if(!ChunkUtils.hasAny(event.chunk(), Blocks.CAVE_AIR)) return;
            dungeonScanner.scanChunk(event.chunk());
        }

        if(showAllTouches.get()) {
            MeteorExecutor.execute(() -> TouchScanner.scan(event.chunk(), this::onTouchFinding));
        }
    }


    @Override
    public void onActivate() {
        dungeonOpts.renderDistance = range;
        dungeonOpts.sideColor = dungeonBoxColor;
        dungeonOpts.lineColor = dungeonLineColor;
        dungeonOpts.tracer = dungeonTracer;
        dungeonOpts.tracerColor = dungeonTracerColor;
        dungeonScanner = new DungeonScanner(this::onDungeonFinding);

        minecartOpts.renderDistance = range;
        minecartOpts.sideColor = minecartBoxColor;
        minecartOpts.lineColor = minecartLineColor;
        minecartOpts.tracer = minecartTracer;
        minecartOpts.tracerColor = minecartTracerColor;
        cartScanner = new MineshaftCartScanner(this::onMinecartFinding);

        touchAirOpts.renderDistance = range;
        touchAirOpts.sideColor = airBlockSideColor;
        touchAirOpts.lineColor = airBlockLineColor;
        touchAirOpts.tracer = airBlockTracer;
        touchAirOpts.tracerColor = airBlockTracerColor;

        touchCaveAirOpts.renderDistance = range;
        touchCaveAirOpts.sideColor = touchCaveAirSideColor;
        touchCaveAirOpts.lineColor = touchCaveAirLineColor;
        touchCaveAirOpts.tracer = new MockSetting<Boolean>(false);

        for(var c : Utils.chunks(true)) {
            onChunkData(new ChunkDataEvent((WorldChunk) c));
        }
    }

    /**
     * Callback for when the dungeon scanner has a positive finding
     * @param finding
     */
    private void onDungeonFinding(DungeonScanFinding finding) {
        finding.getMissingCaveAir().forEach(p -> esp.Block(p, dungeonOpts));

        if (chatNotifications.get()) {
            if(!esp.isNew(finding.getSpawner().getPos())) return;

            var count = finding.getMissingCaveAir().size();
            var coords = ChatUtils.formatCoords(Vec3d.of(finding.getSpawner().getPos()));
            var message = Text.literal(String.format("Dungeon [%d block] at ", count)).append(coords);
            ChatUtils.sendMsg(chatPrefix, message);
        }
    }

    /**
     * Callback for when the minecart scanner has a positive finding
     * @param finding
     */
    private void onMinecartFinding(MinecartScanFinding finding) {
        finding.getMissingCaveAir().forEach(p -> esp.Block(p, minecartOpts));

        if (chatNotifications.get()) {
            if(!esp.isNew(finding.getEntity().getBlockPos())) return;

            var count = finding.getMissingCaveAir().size();
            var coords = ChatUtils.formatCoords(finding.getEntity().getPos());
            var message = Text.literal(String.format("Minecart [%d block] at ", count)).append(coords);
            ChatUtils.sendMsg(chatPrefix, message);
        }
    }

    /**
     * Callback for when the touch scanner has a positive finding
     * @param finding
     */
    private void onTouchFinding(TouchScanFinding finding) {
        var count = finding.getTouching().size();
        if(count < touchThreshold.get()) return;

        if(ignoreFullySurrounded.get() && count == 6) return;
        if(!esp.isNew(finding.getTouched())) return;

        esp.Block(finding.getTouched(), touchAirOpts);
        if(showCaveAir.get()) {
            finding.getTouching().forEach(p -> esp.Block(p, touchCaveAirOpts));
        }
    }


    @Override
    public void onDeactivate() {
        esp.clear();
        dungeonScanner.clear();
        scannedCarts.clear();
    }
}
