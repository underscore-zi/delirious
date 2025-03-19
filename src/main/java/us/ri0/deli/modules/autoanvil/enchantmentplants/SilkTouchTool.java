package us.ri0.deli.modules.autoanvil.enchantmentplants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

import java.util.Arrays;
import java.util.List;

public class SilkTouchTool extends IEnchantmentPlan {
    @Override
    public List<RegistryKey<Enchantment>> RequiredBooks() {
        return Arrays.asList(
            Enchantments.EFFICIENCY
            ,Enchantments.UNBREAKING
            ,Enchantments.SILK_TOUCH
            ,Enchantments.MENDING
        );
    }

    @Override
    public List<Pair<Integer, Integer>> Plan() {
        return Arrays.asList(
            new Pair<>(initialItemID(), bookID(Enchantments.EFFICIENCY))
            , new Pair<>(bookID(Enchantments.UNBREAKING), bookID(Enchantments.MENDING))
            , new Pair<>(resultOfStep(0), resultOfStep(1))
            , new Pair<>(resultOfStep(2), bookID(Enchantments.SILK_TOUCH))
        );
    }
}
