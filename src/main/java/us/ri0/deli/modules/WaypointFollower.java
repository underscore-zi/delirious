package us.ri0.deli.modules;

import baritone.api.BaritoneAPI;
import baritone.api.utils.BetterBlockPos;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.dimension.DimensionTypes;
import us.ri0.deli.Addon;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.world.MinimapWorld;

import java.util.*;

public class WaypointFollower extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> landWhenFinished = sgGeneral.add(new BoolSetting.Builder()
        .name("land-when-finished")
        .description("Attempt to land when there are no more waypoints. Otherwise will hover at last waypoint.")
        .defaultValue(true)
        .build()
    );

    private WaypointIterator iter = null;
    private long lastTs = 0;

    public WaypointFollower() {
        super(Addon.CATEGORY, "waypoint-follower", "Follows Xaeros Waypoints with elytra");
    }


    private void resetState() {
        lastTs = 0;
        iter = null;
    }

    @Override
    public void onActivate() {
        if(!isElytraLoaded()) {
            info("Baritone Elytra is not loaded.");
            toggle();
            return;
        }

        if(isElytraActive()) {
            info("A Baritone elytra process is already running. Please #stop it first.");
            toggle();
            return;
        }
        resetState();

        var wps = getTemporaryWaypoints(0);
        if(wps.isEmpty()) {
            info("No temporary waypoints found.");
            toggle();
            return;
        }

        iter = new WaypointIterator(wps.values());
        lastTs = wps.lastKey();
        elytraPathTo(iter);
    }


    private int remainingDelay = 20;
    @EventHandler
    public void onTick(TickEvent.Post event) {
        if(!isElytraActive()) {
            toggle();
        }

        // Only scan for new waypoints once a second
        if(--remainingDelay > 0) return;
        remainingDelay = 20;

        var wps = getTemporaryWaypoints(lastTs);
        if(wps.isEmpty()) return;
        lastTs = wps.lastKey();
        iter.enqueue(wps.values());
    }

    @Override
    public void onDeactivate() {
        clearTemporaryWaypoints();
        if(isElytraActive()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("stop");
        }
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


    private int getYLevel() {
        var ctx = BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext();
        var isNether = ctx.world().getDimensionEntry().getKey().get() == DimensionTypes.THE_NETHER;
        var allowAboveRoof = BaritoneAPI.getSettings().elytraAllowAboveRoof.value;
        var allowAboveBuild = BaritoneAPI.getSettings().elytraAllowAboveBuildLimit.value;
        if(isNether) {
            if(allowAboveRoof) {
                if(allowAboveBuild) {
                    return 275; // world() doesn't have a maxY() method?
                } else if (ctx.playerFeet().getY() > 128) {
                    return ctx.playerFeet().getY();
                }
            }
            return 65;
        }
        return allowAboveBuild ? 336 : 65;
    }

    private  Iterable<xaero.hud.minimap.waypoint.set.WaypointSet> getWaypointSets() {
        var s = BuiltInHudModules.MINIMAP.getCurrentSession();
        if(s == null) return Collections.emptyList();

        var world = s.getWorldManager().getCurrentWorld();
        if(world == null) return Collections.emptyList();

        return world.getIterableWaypointSets();
    }

    private SortedMap<Long, BetterBlockPos> getTemporaryWaypoints(long afterTs) {
        var world = getXaerosWorld();
        if(world == null) return Collections.emptySortedMap();
        var sets = world.getIterableWaypointSets();

        SortedMap<Long, BetterBlockPos> wps = new TreeMap<>();
        for(var set : sets) {
            for(var wp : set.getWaypoints()) {

                // This is to try and ensure we only follow waypoints added by the quick add feature
                if(!wp.isTemporary()) continue;
                if(!wp.getName().equals("Waypoint")) continue;
                if(!wp.getInitials().equals("X")) continue;

                var ts = wp.getCreatedAt();
                if(ts > afterTs) {
                    wps.put(ts, new BetterBlockPos(wp.getX(), getYLevel(), wp.getZ()));
                }
            }
        }

        return wps;
    }

    private void clearTemporaryWaypoints() {
        var world = getXaerosWorld();
        if(world == null) return;
        var sets = world.getIterableWaypointSets();

        for(var set : sets) {
            var toRemove = new ArrayList<Waypoint>();
            for(var wp : set.getWaypoints()) {
                if(wp.isTemporary()) {
                    toRemove.add(wp);
                }
            }
            for(var wp : toRemove) {
                set.remove(wp);
            }
        }
    }

    private MinimapWorld getXaerosWorld() {
        var s = BuiltInHudModules.MINIMAP.getCurrentSession();
        if(s == null) return null;

        return s.getWorldManager().getCurrentWorld();
    }

    private class WaypointIterator implements Iterator<BetterBlockPos> {
        Queue<BetterBlockPos> waypoints = new LinkedList<>();
        BetterBlockPos last = null;

        public WaypointIterator(Collection<BetterBlockPos> waypoints) {
            this.waypoints.addAll(waypoints);
        }

        public void enqueue(Collection<BetterBlockPos> wps) {
            waypoints.addAll(wps);
        }

        public void enqueue(BetterBlockPos wp) {
            waypoints.add(wp);
        }

        @Override
        public boolean hasNext() {
            return !waypoints.isEmpty() || !landWhenFinished.get();
        }

        @Override
        public BetterBlockPos next() {
            if(!landWhenFinished.get() && waypoints.isEmpty()) {
                return last;
            }

            last = waypoints.poll();
            if(waypoints.isEmpty() && landWhenFinished.get()) {
                return new BetterBlockPos(last.getX(), 65, last.getZ());
            }
            return last;
        }
    }
}
