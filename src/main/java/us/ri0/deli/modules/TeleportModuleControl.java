package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import us.ri0.deli.Addon;

import java.util.List;

public class TeleportModuleControl extends Module {
    public TeleportModuleControl() {
        super(Addon.CATEGORY, "teleport-module-control", "Turn off modules when you teleport");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Module>> modules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("modules")
        .description("Modules to forcibly disable on teleport.")
        .build()
    );

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket pkt) {
            var deltaX = Math.abs(pkt.change().position().getX());
            var deltaZ = Math.abs(pkt.change().position().getZ());

            if (deltaX < 512 && deltaZ < 512) {
                return;
            }

            var count = disableModules();
            if (count > 0) {
                ChatUtils.info("Teleported, disabled " + count + " modules.");
            }
        }
    }

    private int disableModules() {
        int count = 0;
        for (Module module : modules.get()) {
            if (module.isActive()) {
                count++;
                module.toggle();
            }
        }
        return count;
    }
}
