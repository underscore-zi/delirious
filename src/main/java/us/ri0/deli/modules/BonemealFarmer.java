package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import us.ri0.deli.Addon;

public class BonemealFarmer extends Module {
    public BonemealFarmer() {
        super(Addon.CATEGORY, "bonemeal-farmer", "Plant. Bonemeal. Harvest. Repeat.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("tick-delay").description("Delay in ticks between actions.")
        .min(1).sliderMax(20).defaultValue(10)
        .build()
    );

    private int cooldown = 0;

    @Override
    public void onActivate() {
        cooldown = delay.get();
    }

    @Override
    public void onDeactivate() {

    }

    @EventHandler
    public void onGameJoin(GameJoinedEvent event) {
        this.toggle();
    }

    @EventHandler
    private void onGameLeave(GameLeftEvent event) {
        this.toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(cooldown > 0) {
            cooldown--;
            return;
        }

        onTick0();
        cooldown = delay.get();
    }

    private static final Item[] supportedSeeds = new Item[] {
        Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS
    };
    private void onTick0() {
        if (mc.player == null) return;



        var facing = mc.player.getHorizontalFacing();
        var pos = mc.player.getBlockPos().down().offset(facing, 1);
        var block = mc.world.getBlockState(pos).getBlock();
        if(block != Blocks.FARMLAND) {
            this.toggle();
            ChatUtils.sendMsg(Text.literal("Not facing farmland"));
            return;
        }

        var cropPos = pos.up();
        var cropState = mc.world.getBlockState(cropPos);
        if(cropState.getBlock() instanceof CropBlock cropBlock) {
            if(cropBlock.isMature(cropState)) {
                BlockUtils.breakBlock(cropPos, true);
                return;
            } else {
                // Check active item
                if(mc.player.getMainHandStack().getItem() != Items.BONE_MEAL) {
                    // Get Bonemeal from hotbar
                    var res = InvUtils.findInHotbar(Items.BONE_MEAL);
                    if(!res.found()) {
                        ChatUtils.sendMsg(Text.literal("No bonemeal in hotbar"));
                        this.toggle();
                        return;
                    }

                    InvUtils.swap(res.slot(), false);
                }

                BlockUtils.interact(new BlockHitResult(
                    new Vec3d(cropPos.getX(), cropPos.getY(), cropPos.getZ()),
                    Direction.UP,
                    cropPos,
                    false
                ), Hand.MAIN_HAND, false);
            }
        } else {
            // Time to plant
            FindItemResult res = new FindItemResult(-1, 0);
            for(Item seed : supportedSeeds) {
                res = InvUtils.findInHotbar(seed);
                if(res.found()) break;
            }

            if(!res.found()) {
                ChatUtils.sendMsg(Text.literal("No seeds in hotbar"));
                this.toggle();
                return;
            }
            BlockUtils.place(cropPos, res, 100);
        }
    }


}
