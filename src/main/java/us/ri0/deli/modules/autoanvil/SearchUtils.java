package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchUtils {


    public static FindItemResult findBook(RegistryKey<Enchantment> book) {
        return findBook(List.of(book), SlotUtils.HOTBAR_START, SlotUtils.MAIN_END);
    }

    public static FindItemResult findBook(RegistryKey<Enchantment> book, int start, int end) {
        return findBook(List.of(book), start, end);
    }

    public static FindItemResult findBook(List<RegistryKey<Enchantment>> books, int start, int end) {
        return InvUtils.find((x) -> {
            if (x.getItem() != Items.ENCHANTED_BOOK) return false;

            ItemEnchantmentsComponent ements = EnchantmentHelper.getEnchantments(x);

            // We only want exact matches, all the enchants we want, none we don't
            if(ements.getEnchantments().size() != books.size()) return false;

            Set<RegistryKey<Enchantment>> enchantments = ements.getEnchantments().stream()
                .map(entry -> entry.getKey().get())
                .collect(Collectors.toSet());


            return enchantments.containsAll(books);
        }, start, end);

    }

    public static boolean hasBooks(List<RegistryKey<Enchantment>> books) {
        for (var book: books) {
            var res = findBook(book);
            if(!res.found()) return false;
        }
        return true;
    }

    public static FindItemResult findMatchingItem(ItemStack target, int start, int end) {
        return InvUtils.find((stack) -> {
            return areStacksEquals(stack, target);
        }, start, end);
    }

    public static FindItemResult findMatchingItem(ItemStack target) {
        return findMatchingItem(target, SlotUtils.HOTBAR_START, SlotUtils.MAIN_END);
    }

    public static boolean areStacksEquals(ItemStack left, ItemStack right) {
        if(!ItemStack.areItemsEqual(left, right)) return false;

        var leftEnchants = EnchantmentHelper.getEnchantments(left).getEnchantments().stream()
            .map(entry -> entry.getKey().get())
            .collect(Collectors.toSet());
        var rightEnchants = EnchantmentHelper.getEnchantments(right).getEnchantments().stream()
            .map(entry -> entry.getKey().get())
            .collect(Collectors.toSet());

        return leftEnchants.size() == rightEnchants.size()
            && leftEnchants.containsAll(rightEnchants);
    }

}
