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

}
