package us.ri0.deli.modules.caveair;

import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Blocks;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.world.chunk.Chunk;
import us.ri0.deli.chunkutils.ChunkUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;
import us.ri0.deli.esp.MockSetting;

public class DebugScans {

    public static void highlightCaveAir(Esp esp, Chunk chunk) {
        if (!ChunkUtils.hasAny(chunk, Blocks.CAVE_AIR)) return;
        var sections = chunk.getSectionArray();

        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);

        for (var i = 0; i < sections.length; i++) {
            var section = sections[i];
            if (!section.hasAny(BlockPredicate.make(Blocks.CAVE_AIR))) continue;

            var caveAirEsp = new EspOptions();
            caveAirEsp.tracer = new MockSetting<Boolean>(false);
            caveAirEsp.lineColor = new MockSetting<SettingColor>(new SettingColor(r,g,b));
            caveAirEsp.sideColor = new MockSetting<SettingColor>(new SettingColor(r,g,b,2));

            for (var pos : ChunkUtils.positionsOf(chunk, i, Blocks.CAVE_AIR)) {
                esp.Block(pos, caveAirEsp);
            }
        }
    }

    public static void touchPoints(Esp esp, Chunk chunk) {
        if (!ChunkUtils.hasAny(chunk, Blocks.CAVE_AIR)) return;

        var touchingEsp = new EspOptions();
        touchingEsp.tracer = new MockSetting<Boolean>(false);
        touchingEsp.lineColor = new MockSetting<SettingColor>(new SettingColor(0,0,255));
        touchingEsp.sideColor = new MockSetting<SettingColor>(new SettingColor(0,0,255,30));

        var touchedEsp = new EspOptions();
        touchedEsp.tracer = new MockSetting<Boolean>(false);
        touchedEsp.lineColor = new MockSetting<SettingColor>(new SettingColor(0,255,0));
        touchedEsp.sideColor = new MockSetting<SettingColor>(new SettingColor(0,255,0,30));

        var sections = chunk.getSectionArray();
        for (var i = 0; i < sections.length; i++) {
            var section = sections[i];
            for (var x = 1; x < 15; x++) {
                for (var y = 1; y < 15; y++) {
                    for (var z = 1; z < 15; z++) {

                        var pos = ChunkUtils.positionFor(chunk, i, x, y, z);
                        if(section.getBlockState(x, y+1, z).getBlock().equals(Blocks.AIR)) continue;
                        if (section.getBlockState(x, y, z).getBlock().equals(Blocks.AIR)) {
                            int touchPoints = 0;
                            if (section.getBlockState(x+1, y, z).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x-1, y, z).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x, y+1, z).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x, y-1, z).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x, y, z+1).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x, y, z-1).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;

                            if (touchPoints == 5 || touchPoints == 4) {
                                esp.Block(pos, touchingEsp);

                                if (section.getBlockState(x+1, y, z).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x+1, y, z), touchedEsp);
                                if (section.getBlockState(x-1, y, z).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x-1, y, z), touchedEsp);
                                if (section.getBlockState(x, y+1, z).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x, y+1, z), touchedEsp);
                                if (section.getBlockState(x, y-1, z).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x, y-1, z), touchedEsp);
                                if (section.getBlockState(x, y, z+1).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x, y, z+1), touchedEsp);
                                if (section.getBlockState(x, y, z-1).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x, y, z-1), touchedEsp);

                            }
                        }

                    }
                }
            }
        }
    }

}
