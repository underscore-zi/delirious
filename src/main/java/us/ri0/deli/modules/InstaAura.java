package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import us.ri0.deli.Addon;

import java.util.List;
public class InstaAura extends Module {
    public InstaAura() {
        super(Addon.CATEGORY, "InstaAura","Automatically target instabreakable blocks (or maps) around you.");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Blocks to break for.")
            .filter((block) -> {
                try {
                    return block.getHardness() == 0 && block != Blocks.AIR && block != Blocks.VOID_AIR;
                } catch (Exception e) {
                    return false;
                }
            })
        .build()
    );

    private final Setting<Boolean> targetMaps = sgGeneral.add(new BoolSetting.Builder()
        .name("target-maps")
        .description("Targets maps.")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        for(int x = -4; x <= 4; x++) {
            for(int y = -4; y <= 4; y++) {
                for(int z = -4; z <= 4; z++) {
                    var pos = mc.player.getBlockPos().add(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if(blocks.get().contains(block) && BlockUtils.canInstaBreak(mc.player.getBlockPos().add(x, y, z))) {
                        BlockUtils.breakBlock(pos, false);
                    }
                }
            }
        }

        if(targetMaps.get()) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ItemFrameEntity itemFrame && itemFrame.containsMap()) {
                    var pos = itemFrame.getBlockPos();
                    var playerPos = mc.player.getBlockPos();

                    if (Math.abs(pos.getX() - playerPos.getX()) > 4 || Math.abs(pos.getY() - playerPos.getY()) > 4 || Math.abs(pos.getZ() - playerPos.getZ()) > 4) {
                        continue;
                    }

                    mc.interactionManager.attackEntity(mc.player, itemFrame);
                }
            }
        }

    }

}
