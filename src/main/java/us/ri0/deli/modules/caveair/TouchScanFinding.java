package us.ri0.deli.modules.caveair;

import net.minecraft.util.math.BlockPos;

import java.util.HashSet;

public class TouchScanFinding {
    private BlockPos touched;
    private HashSet<BlockPos> touching;

    public TouchScanFinding(BlockPos touched, HashSet<BlockPos> touching) {
        this.touched = touched;
        this.touching = touching;
    }

    public BlockPos getTouched() {
        return touched;
    }

    public HashSet<BlockPos> getTouching() {
        return touching;
    }
}
