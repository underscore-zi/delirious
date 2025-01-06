package us.ri0.deli.esp;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.concurrent.ConcurrentHashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Esp {
    private final ConcurrentHashMap<BlockPos, EspOptions> blocks = new ConcurrentHashMap<BlockPos, EspOptions>();
    private final ConcurrentHashMap<Integer, EspOptions> entities = new ConcurrentHashMap<Integer, EspOptions>();

    public void Block(BlockPos pos, EspOptions opts) {
        blocks.put(pos, opts);
    }

    public boolean isNew(int id) {
        return !entities.containsKey(id);
    }
    public boolean isNew(net.minecraft.entity.Entity entity) {
        return isNew(entity.getId());
    }

    public boolean isNew(BlockPos pos) {
        return !blocks.containsKey(pos);
    }

    public void Entity(int id, EspOptions opts) {
        if(entities.containsKey(id)) return;
        entities.put(id, opts);
    }

    public void Entity(net.minecraft.entity.Entity entity, EspOptions opts) {
        Entity(entity.getId(), opts);
    }

    public void onRender3D(Render3DEvent event) {
        blocks.forEach((pos, opts) -> {
            event.renderer.box(pos, opts.sideColor.get(), opts.lineColor.get(), opts.mode.get(), opts.excludeDir);
            if(opts.tracer.get()) {
                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, opts.tracerColor.get());
            }
        });

        entities.forEach((id, opts) -> {
            net.minecraft.entity.Entity entity = mc.world.getEntityById(id);
            if(entity == null) {
                entities.remove(id);
                return;
            }

            double x = net.minecraft.util.math.MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
            double y = net.minecraft.util.math.MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
            double z = net.minecraft.util.math.MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();

            Box box = entity.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, opts.sideColor.get(), opts.lineColor.get(), ShapeMode.Both, opts.excludeDir);

            if(opts.tracer.get()) {
                double x2 = x + (box.minX + box.maxX) / 2;
                double y2 = y + (box.minY + box.maxY) / 2;
                double z2 = z + (box.minZ + box.maxZ) / 2;

                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, x2, y2, z2, opts.tracerColor.get());
            }
        });

        MeteorExecutor.execute(this::prune);
    }

    public void prune() {
        blocks.entrySet().removeIf(entry -> mc.player.getBlockPos().getChebyshevDistance(entry.getKey().withY(mc.player.getBlockPos().getY())) > entry.getValue().renderDistance.get() * 16);
    }

    public void clear() {
        blocks.clear();
        entities.clear();
    }


}
