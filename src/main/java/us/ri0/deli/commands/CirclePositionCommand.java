package us.ri0.deli.commands;

import baritone.api.BaritoneAPI;
import baritone.api.process.IElytraProcess;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import us.ri0.deli.modules.CirclePosition;

/**
 * The Meteor Client command API uses the <a href="https://github.com/Mojang/brigadier">same command system as Minecraft does</a>.
 */
public class CirclePositionCommand extends Command {
    /**
     * The {@code name} parameter should be in kebab-case.
     */
    public CirclePositionCommand() {
        super("circle", "Sets Baritone elytra to circle a given coordinate");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("x", IntegerArgumentType.integer()).then(argument("z", IntegerArgumentType.integer()).executes(context -> {
            var xPos = IntegerArgumentType.getInteger(context, "x");
            var zPos = IntegerArgumentType.getInteger(context, "z");

            var m = Modules.get().get(CirclePosition.class);
            m.xPos.set(xPos);
            m.zPos.set(zPos);

            if (!m.isActive()) {
                m.toggle();
            }

            return SINGLE_SUCCESS;
        })));
    }
}
