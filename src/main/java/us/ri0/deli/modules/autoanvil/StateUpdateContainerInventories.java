package us.ri0.deli.modules.autoanvil;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class StateUpdateContainerInventories implements IState {
    private IState next;
    private boolean filledQueue;
    private Queue<BlockPos> containers;

    public StateUpdateContainerInventories(IState next) {
        this.next = next;
        this.filledQueue = false;
        this.containers = new LinkedList<>();
    }

    @Override
    public IState next(Context ctx) {
        if(!filledQueue) {
            if(ctx.useChests()) {
                var chests = nearbyChests(ctx.reachDistance());
                containers.addAll(chests.keySet());
            }
            if(ctx.useShulkers()) {
                var shulkers = nearbyShulkers(ctx.reachDistance());
                containers.addAll(shulkers.keySet());
            }

            filledQueue = true;
            return this;
        } else if(!containers.isEmpty()) {
            var pos = containers.poll();
            if(!ctx.inventories.containsKey(pos)) {
                return new StateOpenContainer(pos, true, new StateCloseScreen(this));
            }
            return this;
        }
        return next;
    }

    private Map<BlockPos, ChestBlockEntity> nearbyChests(int radius) {
        var out = new HashMap<BlockPos, ChestBlockEntity>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    var pos = mc.player.getBlockPos().add(x, y, z);
                    var blockState = mc.world.getBlockState(pos);
                    var entity = mc.world.getBlockEntity(pos);

                    if(entity == null) continue;

                    if(entity instanceof ChestBlockEntity chest) {
                        ChestType chestType = blockState.get(ChestBlock.CHEST_TYPE);
                        if (chestType == ChestType.LEFT || chestType == ChestType.RIGHT) {
                            // Double chest
                            var facing = blockState.get(ChestBlock.FACING);
                            var otherPos = pos.offset(chestType == ChestType.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());

                            // Don't add unopenable checks
                            var isBlocked = ChestBlock.isChestBlocked(mc.world, pos);
                            var isOtherBlocked = ChestBlock.isChestBlocked(mc.world, otherPos);
                            if(isBlocked || isOtherBlocked) continue;

                            // And don't add if the other half is already in the list
                            if(out.containsKey(otherPos)) continue;

                            out.put(pos, chest);

                        } else {
                            var isBlocked = ChestBlock.isChestBlocked(mc.world, pos);
                            if(isBlocked) continue;
                            out.put(pos, chest);
                        }
                    }
                }
            }
        }
        return out;
    }

    private Map<BlockPos, ShulkerBoxBlockEntity> nearbyShulkers(int radius) {
        var out = new HashMap<BlockPos, ShulkerBoxBlockEntity>();
        for(int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    var pos = mc.player.getBlockPos().add(x, y, z);
                    var blockState = mc.world.getBlockState(pos);
                    var entity = mc.world.getBlockEntity(pos);
                    if(entity == null) continue;

                    if(entity instanceof ShulkerBoxBlockEntity chest) {
                        if(!isShulkerBlocked(pos, blockState)) {
                            out.put(pos, chest);
                        }
                    }
                }
            }
        }
        return out;
    }

    private boolean isShulkerBlocked(BlockPos pos, BlockState state) {
        var facing = state.get(ShulkerBoxBlock.FACING);
        var otherPos = pos.offset(facing);
        var otherState = mc.world.getBlockState(otherPos);
        if(!otherState.isAir()) return true;
        return false;
    }






}
