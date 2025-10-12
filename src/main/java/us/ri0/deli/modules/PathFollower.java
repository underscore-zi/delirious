package us.ri0.deli.modules;

import baritone.api.BaritoneAPI;
import baritone.api.utils.BetterBlockPos;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import us.ri0.deli.Addon;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.world.MinimapWorld;

import java.util.Iterator;
import java.util.List;

public class PathFollower extends Module {
    public PathFollower() {
        super(Addon.CATEGORY, "path-follower", "Follows a predefined path with an elytra.");
    }

    public static final String WP_PREFIX = "[PF] ";

    List<BetterBlockPos> path = null;
    PathIterator iter = null;

    public SettingGroup sgGeneral = settings.getDefaultGroup();
    public Setting<String> filePath = sgGeneral.add(new StringSetting.Builder()
        .name("file-path")
        .description("Path to the waypoint file.")
        .defaultValue("path.txt")
            .onChanged(v -> {
                if (isActive()) {
                    ChatUtils.error("Path was changed, disabling current flight.");
                    toggle();
                }
                unloadPath();
            })
        .build()
    );

    @Override
    public void onActivate() {
        if(!isElytraLoaded()) {
            ChatUtils.error("Baritone Elytra is not loaded.");
            toggle();
            return;
        }

        if(isElytraActive()) {
            ChatUtils.error("A Baritone elytra process is already running. Please #stop it first.");
            toggle();
            return;
        }

        if(path == null) {
            if(!loadPathFromFile(filePath.get())) {
                toggle();
                return;
            }
        }

        if(iter == null) {
            iter = new PathIterator(path, 0);
        } else {
            // Step the iterator back one step so it reattempts the last reported position as this is a resumption
            ChatUtils.infoPrefix("PathFollower", "Resuming previously started path.");
            iter.back();
        }

        elytraPathTo(iter);
        createWaypoints();
    }

    @Override
    public void onDeactivate() {
        if(isElytraActive()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("stop");
        }
        removeWaypoints();

    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if(!isElytraActive()) {
            if (iter == null || iter.hasNext()) {
                ChatUtils.infoPrefix("PathFollower", "Elytra process stopped unexpectedly. Restart module to resume flight.");
                toggle();
            } else {
                ChatUtils.infoPrefix("PathFollower", "Flight complete :)");
                unloadPath();
                toggle();
            }
        }
    }

    private void unloadPath() {
        path = null;
        iter = null;
    }

    private boolean isElytraLoaded() {
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if(baritone == null) return false;
        return baritone.getElytraProcess().isLoaded();
    }
    private boolean isElytraActive() {
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if(baritone == null) return false;
        return baritone.getElytraProcess().isActive();
    }

    private void elytraPathTo(Iterator<BetterBlockPos> iter) {
        var elytra = BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess();
        elytra.pathTo(iter);
    }


    private MinimapWorld getXaerosWorld() {
        var s = BuiltInHudModules.MINIMAP.getCurrentSession();
        if(s == null) return null;

        return s.getWorldManager().getCurrentWorld();
    }

    private void removeWaypoints() {
        var world = getXaerosWorld();
        if(world == null) {
            return;
        }

        var set = world.getCurrentWaypointSet();
        var wps = set.getWaypoints().iterator();
        while(wps.hasNext()) {
            var wp = wps.next();
            if(wp.getName().startsWith(WP_PREFIX)) {
                wps.remove();
            }
        }
    }

    private void createWaypoints() {
        if(path == null) {
            return;
        }

        var world = getXaerosWorld();
        if(world == null) {
            return;
        }

        var set = world.getCurrentWaypointSet();

        for (int i = 0; i < path.size(); i++) {
            var pos = path.get(i);
            set.add(new Waypoint(
                pos.getX(), pos.getY(), pos.getZ(),
                String.format("%s%d", WP_PREFIX, i ),
                String.format("%d", i % 1000), // Xaeros cuts off after 3 characters in initial,
                WaypointColor.GRAY,
                WaypointPurpose.NORMAL, true)
            );
        }

    }

    private boolean loadPathFromFile(String filePath) {
        var world = getXaerosWorld();
        if(world == null) {
            return false;
        }

        // Check if file exists
        var fp = new java.io.File(filePath);
        try(var scanner = new java.util.Scanner(fp)) {
            var positions = new java.util.ArrayList<BetterBlockPos>();
            if(!scanner.hasNextLine()) {
                ChatUtils.error("Path file is empty: %s", filePath);
                return false;
            }
            var dimension = scanner.nextLine().trim();
            if(dimension.isEmpty() || dimension.startsWith("#")) {
                ChatUtils.error("First line of path file must specify dimension (e.g. overworld, the_nether, the_end)");
                return false;
            }

            if(!mc.world.getDimensionEntry().getIdAsString().toLowerCase().contains(dimension.toLowerCase())) {
                ChatUtils.error("Current dimension (%s) does not match path file dimension (%s)", mc.world.getDimensionEntry().getIdAsString(), dimension);
                return false;
            }

            while(scanner.hasNextLine()) {
                var line = scanner.nextLine().trim();
                if(line.isEmpty() || line.startsWith("#")) continue; // skip empty lines and comments
                var parts = line.split(" ");
                if(parts.length != 3) {
                    ChatUtils.error("Invalid line in path file: %s", line);
                    return false;
                }
                try {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    int z = Integer.parseInt(parts[2].trim());
                    positions.add(new BetterBlockPos(x, y, z));
                } catch(NumberFormatException e) {
                    ChatUtils.error("Invalid coordinates in line: %s", line);
                    return false;
                }
            }
            if(positions.isEmpty()) {
                ChatUtils.error("No valid positions found in path file.");
                return false;
            }
            this.path = positions;
            ChatUtils.info("Loaded %d waypoints", positions.size(), filePath);
            this.iter = null;
            return true;
        } catch(java.io.FileNotFoundException e) {
            ChatUtils.error("File not found: %s", e.getMessage());
            return false;
        }
    }

    private static class PathIterator implements Iterator<BetterBlockPos> {
        private final List<BetterBlockPos> path;
        private int index = 0;

        public PathIterator(List<BetterBlockPos> path, int startIndex) {
            this.path = path;
            this.index = startIndex;
        }

        public void back() {
            if(index > 0) index--;
        }

        public boolean hasNext() {
            return index < path.size();
        }

        public BetterBlockPos next() {
            return path.get(index++);
        }
    }
}
