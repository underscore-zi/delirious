package us.ri0.deli.modules.caveair;

import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;

public class MinecartScanFinding {
    private ChestMinecartEntity entity;
    private HashSet<BlockPos> missingCaveAir;
    private HashSet<BlockPos> caveAir;

    MinecartScanFinding(ChestMinecartEntity entity, HashSet<BlockPos> missingCaveAir, HashSet<BlockPos> caveAir) {
        this.entity = entity;
        this.missingCaveAir = missingCaveAir;
        this.caveAir = caveAir;
    }

    public ChestMinecartEntity getEntity() {
        return entity;
    }

    public HashSet<BlockPos> getMissingCaveAir() {
        return missingCaveAir;
    }

    public HashSet<BlockPos> getCaveAir() {
        return caveAir;
    }
}
