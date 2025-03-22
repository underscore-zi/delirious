package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.utils.player.SlotUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import us.ri0.deli.modules.autoanvil.enchantmentplants.IEnchantmentPlan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Context {
    public final HashMap<BlockPos, List<ItemStack>> inventories = new HashMap<>();
    private final AutoAnvil module;
    public Context(AutoAnvil module) {
        this.module = module;
    }
    int tickDelay = 0;
    public void addDelay(int delay) {
        tickDelay += delay;
    }
    public int reachDistance() {
        return module.reach.get();
    }
    public boolean useKillAuraForXP() {
        return module.useKillAura.get();
    }

    public boolean useChests() {
        return module.useChests.get();
    }
    public boolean useShulkers() {
        return module.useShulkers.get();
    }

    public boolean dropItemWhenComplete() {
        return module.dropItemWhenComplete.get();
    }


    public void setInventory(BlockPos pos, List<ItemStack> inventory) {
        inventories.put(pos, inventory);
    }


    public List<Item> enchantableItems() {
        LinkedList<Item> items = new LinkedList<>();
        var doDiamond = module.enchantDiamond.get();
        var doNether = module.enchantNetherite.get();

        /* Tools */

        // Pickaxes
        if(module.enchantPickaxes.get()) {
            if(doDiamond) items.add(Items.DIAMOND_PICKAXE);
            if(doNether) items.add(Items.NETHERITE_PICKAXE);
        }

        // Axes
        if(module.enchantAxes.get()) {
            if(doDiamond) items.add(Items.DIAMOND_AXE);
            if(doNether) items.add(Items.NETHERITE_AXE);
        }

        // Shovels
        if(module.enchantShovels.get()) {
            if(doDiamond) items.add(Items.DIAMOND_SHOVEL);
            if(doNether) items.add(Items.NETHERITE_SHOVEL);
        }

        // Hoes
        if(module.enchantHoes.get()) {
            if(doDiamond) items.add(Items.DIAMOND_HOE);
            if(doNether) items.add(Items.NETHERITE_HOE);
        }

        // Shears
        if(module.enchantShears.get()) {
            items.add(Items.SHEARS);
        }

        /* Armor */

        // Helmets
        if(module.enchantHelmets.get()) {
            if(doDiamond) items.add(Items.DIAMOND_HELMET);
            if(doNether) items.add(Items.NETHERITE_HELMET);
        }

        // Chestplates
        if(module.enchantChestplates.get()) {
            if(doDiamond) items.add(Items.DIAMOND_CHESTPLATE);
            if(doNether) items.add(Items.NETHERITE_CHESTPLATE);
        }

        // Leggings
        if(module.enchantLeggings.get()) {
            if(doDiamond) items.add(Items.DIAMOND_LEGGINGS);
            if(doNether) items.add(Items.NETHERITE_LEGGINGS);
        }

        // Boots
        if(module.enchantBoots.get()) {
            if(doDiamond) items.add(Items.DIAMOND_BOOTS);
            if(doNether) items.add(Items.NETHERITE_BOOTS);
        }

        /* Weapons */

        // Swords
        if(module.enchantSwords.get()) {
            if(doDiamond) items.add(Items.DIAMOND_SWORD);
            if(doNether) items.add(Items.NETHERITE_SWORD);
        }

        return items;
    }

    public IEnchantmentPlan planForItem(Item item) {
        // Tools
        if (item == Items.DIAMOND_PICKAXE || item == Items.NETHERITE_PICKAXE) {
            return module.pickaxePlan.get().plan();
        } else if (item == Items.DIAMOND_AXE || item == Items.NETHERITE_AXE) {
            return module.axePlan.get().plan();
        } else if (item == Items.DIAMOND_SHOVEL || item == Items.NETHERITE_SHOVEL) {
            return module.shovelPlan.get().plan();
        } else if (item == Items.DIAMOND_HOE || item == Items.NETHERITE_HOE) {
            return module.hoePlan.get().plan();
        } else if (item == Items.SHEARS) {
            return module.shearsPlan.get().plan();
        }

        // Armor
        if (item == Items.DIAMOND_HELMET || item == Items.NETHERITE_HELMET) {
            return module.helmetPlan.get().plan();
        } else if (item == Items.DIAMOND_CHESTPLATE || item == Items.NETHERITE_CHESTPLATE) {
            return module.chestplatePlan.get().plan();
        } else if (item == Items.DIAMOND_LEGGINGS || item == Items.NETHERITE_LEGGINGS) {
            return module.leggingsPlan.get().plan();
        } else if (item == Items.DIAMOND_BOOTS || item == Items.NETHERITE_BOOTS) {
            return module.bootsPlan.get().plan();
        }

        // Weapons
        if (item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD) {
            return module.swordPlan.get().plan();
        }

        throw new RuntimeException("Item has no plan");
    }


    /**
     *
     * @param target is the item to search for
     * @return a pair indicating if it was found and the position and slot index if it was. The second pair will be null
     * if the item is found in personal inventory and nothing needs to be moved.
     */
    public Pair<Boolean, Pair<BlockPos, Integer>> findInInventories(ItemStack target) {
        if (checkPersonalInventory(target)) {
            return new Pair<>(true, null);
        }

        for (var entry : inventories.entrySet()) {
            var pos = entry.getKey();
            var inventory = entry.getValue();
            for (int i = 0; i < inventory.size(); i++) {
                var stack = inventory.get(i);
                if(SearchUtils.areStacksEquals(stack, target)) {
                    return new Pair<>(true, new Pair<>(pos, i));
                }
            }
        }
        return new Pair<>(false, null);
    }

    public Pair<Boolean, Pair<BlockPos, Integer>> findInInventories(List<RegistryKey<Enchantment>> books) {
        if(checkPersonalInventory(books)) {
            return new Pair<>(true, null);
        }

        for (var entry : inventories.entrySet()) {
            var pos = entry.getKey();
            var inventory = entry.getValue();
            for (int i = 0; i < inventory.size(); i++) {
                var stack = inventory.get(i);
                if(stack.getItem() != Items.ENCHANTED_BOOK) continue;
                var ements = EnchantmentHelper.getEnchantments(stack);
                Set<RegistryKey<Enchantment>> enchantments = ements.getEnchantments().stream()
                    .map(x -> x.getKey().get())
                    .collect(Collectors.toSet());

                if(enchantments.containsAll(books)) {
                    return new Pair<>(true, new Pair<>(pos, i));
                }
            }
        }
        return new Pair<>(false, null);
    }

    private boolean checkPersonalInventory(ItemStack target) {
        return SearchUtils.findMatchingItem(target).found();
    }

    private boolean checkPersonalInventory(List<RegistryKey<Enchantment>> enchants) {
        return SearchUtils.findBook(enchants, SlotUtils.HOTBAR_START, SlotUtils.MAIN_END).found();
    }
}
