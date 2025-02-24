package us.ri0.deli;

import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class TextUtils {
    public static MutableText coords(Vec3d pos) {
        MutableText coordsText = Text.literal(String.format(" %d, %d, %d ", (int)pos.x, (int)pos.y, (int)pos.z));
        Style style = coordsText
            .getStyle()
            .withColor(Formatting.WHITE);
        coordsText.setStyle(style);
        return coordsText;
    }
    public static MutableText circleCommandLink(Vec3d pos) {
        MutableText coordsText = Text.literal(" [circle] ");
        if (BaritoneUtils.IS_AVAILABLE) {
            Style style = coordsText
                .getStyle()
                .withColor(Formatting.WHITE)
                .withFormatting(Formatting.BOLD)
                .withFormatting(Formatting.UNDERLINE)
                .withHoverEvent(
                    new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text.literal("Circle position" ))
                )
                .withClickEvent(
                    new MeteorClickEvent(
                        net.minecraft.text.ClickEvent.Action.RUN_COMMAND,
                        String.format(".circle %d %d", (int)pos.x, (int)pos.z)
                    )
                );
            coordsText.setStyle(style);
        }
        return coordsText;
    }

}
