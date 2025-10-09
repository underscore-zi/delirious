package us.ri0.deli.modules;

import baritone.api.BaritoneAPI;
import baritone.api.utils.BetterBlockPos;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import us.ri0.deli.Addon;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;

import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

public class WaypointFollower extends Module {

    public WaypointFollower() {
        super(Addon.CATEGORY, "waypoint-follower", "Follows Xaeros Waypoints with elytra");
    }

    private long lastTs = 0;

    @Override
    public void onActivate() {
        lastTs = 0;

        var s = BuiltInHudModules.MINIMAP.getCurrentSession();
        if(s == null) return;

        var world = s.getWorldManager().getCurrentWorld();
        if(world == null) return;

        var sets = world.getIterableWaypointSets();
        SortedMap<Long, BetterBlockPos> wps = new TreeMap<>();
        sets.forEach(set -> {
            set.getWaypoints().forEach(wp -> {
                if(wp.isTemporary()) {
                    var ts = wp.getCreatedAt();
                    if(ts > lastTs) lastTs = ts;
                    wps.put(ts, new BetterBlockPos(wp.getX(), 65, wp.getZ()));
                }
            });
        });

        if(wps.isEmpty()) {
            info("No temporary waypoints found.");
            toggle();
            return;
        }

        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        var elytra = baritone.getElytraProcess();
        if(!elytra.isLoaded()) {
            info("Elytra not loaded.");
            toggle();
        }

        elytra.pathTo(wps.values().iterator());
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        var elytra = baritone.getElytraProcess();
        if(!elytra.isActive()) {
            toggle();
        }


    }

    @Override
    public void onDeactivate() {
        var s = BuiltInHudModules.MINIMAP.getCurrentSession();
        if(s == null) return;

        var world = s.getWorldManager().getCurrentWorld();
        if(world == null) return;

        var sets = world.getIterableWaypointSets();
        sets.forEach(set -> {
            var toRemove = new LinkedList<Waypoint>();
            set.getWaypoints().forEach(wp -> {
                if(wp.isTemporary()) {
                    toRemove.add(wp);
                }
            });
            toRemove.forEach(set::remove);
        });

        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        var elytra = baritone.getElytraProcess();
        if(!elytra.isActive()) {
            baritone.getCommandManager().execute("stop");
        }


    }
}
