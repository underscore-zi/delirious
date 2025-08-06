package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import us.ri0.deli.Addon;

public class ElytraReplace extends Module {
    public ElytraReplace() {
        super(Addon.CATEGORY, "ElytraReplacer", "Replaces broken elytras with new ones from inventory");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> minDurability = sgGeneral.add(new IntSetting.Builder()
        .name("min-durability").description("Durability to swap elytra at.")
        .min(1).sliderMax(432).defaultValue(2)
        .build()
    );

    @Override
    public void onActivate() { }

    @Override
    public void onDeactivate() { }

    @EventHandler
    private void onTick(TickEvent.Post event) {

        if (mc.player == null || mc.world == null) return;

        // Only do anything if we have elytra on in the first place
        var chestStack = mc.player.getInventory().armor.get(2);
        if (!(chestStack.getItem() == Items.ELYTRA )) {
            return;
        }

        // Check if the elytra is broken
        if ((chestStack.getMaxDamage() - chestStack.getDamage()) > minDurability.get()) {
            return;
        }

        // Find a new elytra in the inventory
        var slot = InvUtils.find((stack)-> {
            if( stack.getItem() != Items.ELYTRA) return false;
            int remainingDamage = stack.getMaxDamage() - stack.getDamage();
            return remainingDamage > minDurability.get();
        }, SlotUtils.HOTBAR_START, SlotUtils.MAIN_END);

        if (slot.found()) {
            InvUtils.move().from(slot.slot()).toArmor(2);
        }

    }


}
