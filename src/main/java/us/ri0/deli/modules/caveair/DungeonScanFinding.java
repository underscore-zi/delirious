package us.ri0.deli.modules.caveair;

import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;

public class DungeonScanFinding {
    private MobSpawnerBlockEntity spawner;
    private HashSet<BlockPos> missingCaveAir;
    private HashSet<BlockPos> caveAir;

    DungeonScanFinding(MobSpawnerBlockEntity spawner, HashSet<BlockPos> missingCaveAir, HashSet<BlockPos> caveAir) {
        this.spawner = spawner;
        this.missingCaveAir = missingCaveAir;
        this.caveAir = caveAir;
    }

    public MobSpawnerBlockEntity getSpawner() {
        return spawner;
    }

    public HashSet<BlockPos> getMissingCaveAir() {
        return missingCaveAir;
    }

    public HashSet<BlockPos> getCaveAir() {
        return caveAir;
    }


}
