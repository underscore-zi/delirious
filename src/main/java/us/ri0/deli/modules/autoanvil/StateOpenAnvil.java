package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.AnvilBlock;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

class StateOpenAnvil implements IState {
    IState next;
    private int noAnvilCount = 0;
    private int interactionCount = 0;
    public StateOpenAnvil(IState next) {
        this.next = next;
    }

    public IState next(Context ctx) {
        // Wait for screen to close if any screen other than anvil is open
        if (mc.currentScreen != null && !(mc.currentScreen instanceof AnvilScreen)) {
            return new StateWaitForScreen(null, this);
        }
        if (mc.currentScreen instanceof AnvilScreen) {
            return next;
        }

        var anvilPos = findNearestAnvil(ctx.reachDistance());
        if(anvilPos == null) {
            noAnvilCount++;
            ctx.addDelay(20);
            if(noAnvilCount > 3) {
                return new StateError("Could not find an anvil");
            }
            return this;
        }

        if(interactionCount > 20) {
            return new StateError("Failed to open anvil");
        }

        noAnvilCount = 0; // reset the count
        BlockUtils.interact(new BlockHitResult(
            new Vec3d(anvilPos.getX(), anvilPos.getY(), anvilPos.getZ()),
            Direction.DOWN,
            anvilPos,
            false
        ), Hand.MAIN_HAND, false);
        interactionCount++;
        return this;
    }

    private BlockPos findNearestAnvil(int radius) {
        if(mc.world == null || mc.player == null) return null;
        var playerPos = mc.player.getBlockPos();
        BlockPos anvilPos = null;
        double anvilDistance = Double.MAX_VALUE;

        for(int x = -radius; x < radius; x++) {
            for(int y = -radius; y < radius; y++) {
                for(int z = -radius; z < radius; z++) {
                    var block = mc.world.getBlockState(playerPos.add(x, y, z)).getBlock();
                    if(block instanceof AnvilBlock) {
                        var distance = PlayerUtils.squaredDistanceTo(playerPos.add(x, y, z));
                        if(distance < anvilDistance) {
                            anvilDistance = distance;
                            anvilPos = playerPos.add(x, y, z);
                        }
                    }
                }
            }
        }
        return anvilPos;
    }
}
