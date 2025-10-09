package us.ri0.deli.hud;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import us.ri0.deli.Addon;

import java.util.HashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EntityCounter extends HudElement {
    public static final HudElementInfo<EntityCounter> INFO = new HudElementInfo<>(Addon.HUD_GROUP, "entity-counter", "Displays currently loaded entities.", EntityCounter::new);

    public EntityCounter() {
        super(INFO);
    }
    
    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    private final Setting<Boolean> showItems = sgGeneral.add(new BoolSetting.Builder()
        .name("show-items")
        .description("Show Item entities.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> itemColor = sgGeneral.add(new ColorSetting.Builder()
        .name("item-color")
        .description("Color of items in list")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(showItems::get)
        .build()
    );



    private final Setting<Boolean> showMonsters = sgGeneral.add(new BoolSetting.Builder()
        .name("show-monsters")
        .description("Show Mob entities.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> monsterColor = sgGeneral.add(new ColorSetting.Builder()
        .name("monster-color")
        .description("Color of mobs in list")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(showMonsters::get)
        .build()
    );

    private final Setting<Boolean> showPassive = sgGeneral.add(new BoolSetting.Builder()
        .name("show-passive")
        .description("Show Passive entities.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> passiveColor = sgGeneral.add(new ColorSetting.Builder()
        .name("mob-color")
        .description("Color of passive mobs in list")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(showPassive::get)
        .build()
    );

    private class EntityDisplay {
        String name;
        int count;
        SettingColor color;

        @Override
        public String toString() { return name + ": " + count;}
    }

    @Override
    public void render(HudRenderer renderer) {
        var entities = mc.world.getEntities();
        final HashMap<String, EntityDisplay> counts = new HashMap<>();


        for (var entity : entities) {
            HashMap<String, Integer> target = null;
            SettingColor color = null;
            int count = 1;
            if (entity instanceof ItemEntity itemEntity && showItems.get()) {
                color = itemColor.get();
                count = itemEntity.getStack().getCount();
            }
            if (entity instanceof Monster && showMonsters.get()) {
                color = monsterColor.get();
            }
            if (entity instanceof PassiveEntity && showPassive.get()) {
                color = passiveColor.get();
            }

            if (color == null) {
                continue;
            }

            var name = entity.getName().getString();
            var display = counts.get(name);
            if (display == null) {
                display = new EntityDisplay();
                display.name = name;
                display.color = color;
                counts.put(name, display);
            }
            display.count += count;
        }

        double width = 0;
        for (var entry : counts.entrySet()) {
            String name = entry.getKey();
            width = Math.max(width, renderer.textWidth(name, true));
        }
        width += renderer.textWidth(": 999999", true);
        double height = renderer.textHeight(true) * 1.05 * counts.size();

        setSize(width + renderer.textWidth(": 999999", true), height);

        double offset = y;
        for(var entry : counts.values()) {
            renderer.text(entry.name + ": " + entry.count, x, offset, entry.color, true);
            offset += renderer.textHeight(true) * 1.05;
        }
    }
}
