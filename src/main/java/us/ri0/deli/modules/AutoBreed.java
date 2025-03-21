package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import us.ri0.deli.Addon;

public class AutoBreed extends Module {
    public AutoBreed() {
        super(Addon.CATEGORY, "auto-wheat-breed", "Automatically breeds animals while holding wheat.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("tick-delay").description("Delay in ticks between actions.")
        .min(1).sliderMax(20).defaultValue(3)
        .build()
    );
    private final Setting<Integer> reach = sgGeneral.add(new IntSetting.Builder()
        .name("reach-distance").description("Maximum reach distance.")
        .min(1).sliderMax(10).defaultValue(5)
        .build()
    );

    private int cooldown = 0;
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(cooldown > 0) {
            cooldown--;
            return;
        }
        resetCooldown();
        onTick0();
    }
    private void resetCooldown() {
        cooldown = delay.get();
    }

    private void onTick0() {
        if(mc.player.getMainHandStack().getItem() != net.minecraft.item.Items.WHEAT) return;
        var entities = mc.world.getEntities();
        for(var entity : entities) {
            if(!entity.isInRange(mc.player, reach.get())) continue;

            if(entity instanceof net.minecraft.entity.passive.AnimalEntity animal) {
                if(animal.isBaby() || animal.isInLove()) continue;
                mc.interactionManager.interactEntity(mc.player, animal, net.minecraft.util.Hand.MAIN_HAND);
                return;
            }
        }

    }

}
