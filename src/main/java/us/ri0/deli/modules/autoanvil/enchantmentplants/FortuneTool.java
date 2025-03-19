package us.ri0.deli.modules.autoanvil.enchantmentplants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

import java.util.Arrays;
import java.util.List;

public class FortuneTool extends IEnchantmentPlan {
    @Override
    public List<RegistryKey<Enchantment>> RequiredBooks() {
        return Arrays.asList(
            Enchantments.EFFICIENCY
            ,Enchantments.UNBREAKING
            ,Enchantments.FORTUNE
            ,Enchantments.MENDING
        );
    }

    @Override
    public List<Pair<Integer, Integer>> Plan() {
        return Arrays.asList(
            /* Step 0   */ new Pair<>(initialItemID(), bookID(Enchantments.FORTUNE))
            /* Step 1 */ , new Pair<>(bookID(Enchantments.UNBREAKING), bookID(Enchantments.MENDING))
            /* Step 2 */ , new Pair<>(resultOfStep(0), resultOfStep(1))
            /* Step 3 */ , new Pair<>(resultOfStep(2), bookID(Enchantments.EFFICIENCY))
        );
    }
}
