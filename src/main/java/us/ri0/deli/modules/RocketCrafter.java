package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import us.ri0.deli.Addon;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class RocketCrafter extends Module {
    public RocketCrafter() {
        super(Addon.CATEGORY, "RockerCrafter", "Automatically crafts fd3 rockets");
    }
    final int DELAY_BETWEEN_ACTIONS = 5;
    BlockPos lastShulkerInteract = null;
    BlockPos paperShulker = null;
    BlockPos gpShulker = null;
    @Override
    public void onActivate() {
        // Initialization logic if needed
    }

    @Override
    public void onDeactivate() {
        // Cleanup logic if needed
    }

    boolean screenDone = false;
    int delay = 0;

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        var screen = mc.currentScreen;
        if(screen == null) {
            screenDone = false;
            return;
        }
        if(screenDone) return;

        if(delay --> 0) return;


        if(screen instanceof CraftingScreen craftingScreen) {
            if(craftingScreen.getScreenHandler() instanceof CraftingScreenHandler handler) {
                handleCraftingScreen(craftingScreen, handler);
                return;
            }
        } else if(screen instanceof ShulkerBoxScreen shulkerScreen) {
            if(shulkerScreen.getScreenHandler() instanceof ShulkerBoxScreenHandler handler) {
                if(handleShulker(shulkerScreen, handler)) {
                    screenDone = true;
                    screen.close();
                }
                return;
            }
        }
    }

    public boolean handleCraftingScreen(CraftingScreen screen, CraftingScreenHandler handler) {
        if(areSlotsEmpty(handler.getInputSlots())) {
            handleCrafting(screen, handler);
            return true;
        } else {
            InvUtils.shiftClick().slotId(handler.getOutputSlot().id);
            return true;
        }
    }

    public boolean handleCrafting(CraftingScreen screen, CraftingScreenHandler handler) {
        var sugarCane = findAll(Items.SUGAR_CANE, 64);
        if (sugarCane.size() >= 3) {
            for(int i = 0; i < 3; i++) {
                InvUtils.shiftClick().slot(sugarCane.get(i).slot());
            }
            delay = DELAY_BETWEEN_ACTIONS;
            return true;
        }

        var paper = findAll(Items.PAPER, 64);
        var gunpowder = findAll(Items.GUNPOWDER, 64);
        if(!paper.isEmpty() && gunpowder.size() >= 3) {
            InvUtils.shiftClick().slot(paper.get(0).slot());
            for(int i = 0; i < 3; i++) {
                InvUtils.shiftClick().slot(gunpowder.get(i).slot());
            }
            delay = DELAY_BETWEEN_ACTIONS;
            return true;
        }

        return false;
    }

    public boolean areSlotsEmpty(List<Slot> slots) {
        for (Slot slot : slots) {
            if (slot.hasStack()) {
                return false;
            }
        }
        return true;
    }

    public boolean isShulkerEmpty(List<Slot> slots) {
        for(int i=0;i<27;i++) {
            if(slots.get(i).hasStack()) {
                return false;
            }
        }
        return true;
    }
    public List<FindItemResult> findAll(Item item, int minCount) {
        LinkedList<FindItemResult> results = new LinkedList<>();
        FindItemResult lastResult = null;

        do {
            lastResult = InvUtils.find(
                // BUG: stack.getCount() is actually the count of all stacks in the inventory that were searched so its
                // not doing what you'd expect here

                (Predicate<ItemStack>) (stack) -> stack.getItem() == item && stack.getCount() >= minCount,
                lastResult == null ? SlotUtils.HOTBAR_START : lastResult.slot() + 1,
                SlotUtils.MAIN_END
            );

            if (lastResult.found()) {
                results.add(lastResult);
            }
        } while(lastResult.found());
        return results;
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if(paperShulker != null) {
            event.renderer.box(paperShulker, new Color(200,200,200, 50), new Color(200,200,200, 150), ShapeMode.Both, 0);
        }

        if(gpShulker != null) {
            event.renderer.box(gpShulker, new Color(50,50,50, 50), new Color(50,50,50, 150), ShapeMode.Both, 0);
        }
    }

    public boolean handleShulker(ShulkerBoxScreen screen, ShulkerBoxScreenHandler handler) {
        if (isShulkerEmpty(handler.slots)) {
            // If the shulker is empty then we are probably putting something away
            List<FindItemResult> targets;
            var rockets = findAll(Items.FIREWORK_ROCKET, 64);
            var paper = findAll(Items.PAPER, 64);

            targets = (rockets.size() > paper.size()) ? rockets : paper;

            if (targets == paper && paper.size() <= 9) {
                // Just in case one accidentally opens an empty shulker when looking for gp
                return false;
            }

            for (FindItemResult target : targets) {
                InvUtils.shiftClick().slot(target.slot());
            }
            delay = DELAY_BETWEEN_ACTIONS;
            return true;
        } else {
            // Otherwise we are taking something out of the shulker
            var gunpowder = findInShulker(handler.slots, Items.GUNPOWDER, 64);
            var sugarcane = findInShulker(handler.slots, Items.SUGAR_CANE, 64);
            var paper = findInShulker(handler.slots, Items.PAPER, 64);
            var ownPaper = findAll(Items.PAPER, 64);

            if(gunpowder.size() == 27) {
                for(int i=0;i<27;i++) {
                    InvUtils.shiftClick().slotId(gunpowder.get(i).id);
                }
                gpShulker = lastShulkerInteract;
                delay = DELAY_BETWEEN_ACTIONS * 4;
                return true;
            } else if (sugarcane.size() == 27) {
                for(int i=0;i<27;i++) {
                    InvUtils.shiftClick().slotId(sugarcane.get(i).id);
                }

                paperShulker = lastShulkerInteract;
                delay = DELAY_BETWEEN_ACTIONS * 4;
                return true;
            } else if (paper.size() >= 9 && ownPaper.isEmpty()) {
                for(int i=0;i<9;i++) {
                    InvUtils.shiftClick().slotId(paper.get(i).id);
                }
                paperShulker = lastShulkerInteract;
                delay = DELAY_BETWEEN_ACTIONS * 4;
                return true;
            }
        }
        return false;
    }

    public List<Slot> findInShulker(List<Slot> slots, Item item, int minCount) {
        List<Slot> foundSlots = new LinkedList<>();
        for (Slot slot : slots) {
            if(slot instanceof ShulkerBoxSlot) {
                if (slot.hasStack() && slot.getStack().getItem() == item && slot.getStack().getCount() >= minCount) {
                    foundSlots.add(slot);
                }
            }
        }
        return foundSlots;
    }

    @EventHandler
    public void onInteract(InteractBlockEvent event) {
        var pos = event.result.getBlockPos();
        final Item[] shulkers = {
            Items.SHULKER_BOX,
            Items.BLACK_SHULKER_BOX,
            Items.BLUE_SHULKER_BOX,
            Items.BROWN_SHULKER_BOX,
            Items.CYAN_SHULKER_BOX,
            Items.GRAY_SHULKER_BOX,
            Items.GREEN_SHULKER_BOX,
            Items.LIGHT_BLUE_SHULKER_BOX,
            Items.LIGHT_GRAY_SHULKER_BOX,
            Items.LIME_SHULKER_BOX,
            Items.MAGENTA_SHULKER_BOX,
            Items.ORANGE_SHULKER_BOX,
            Items.PINK_SHULKER_BOX,
            Items.PURPLE_SHULKER_BOX,
            Items.RED_SHULKER_BOX,
            Items.WHITE_SHULKER_BOX,
            Items.YELLOW_SHULKER_BOX
        };

        var item = mc.world.getBlockState(pos).getBlock().asItem();
        for (Item shulker : shulkers) {
            if (item == shulker) {
                lastShulkerInteract = pos;
                return;
            }
        }
    }

    @EventHandler
    public void onBreak(BreakBlockEvent event) {
        var pos = event.blockPos;
        if(pos == gpShulker) {
            gpShulker = null;
        }

        if(pos == paperShulker) {
            paperShulker = null;
        }
    }

}
