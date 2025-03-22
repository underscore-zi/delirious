package us.ri0.deli.modules.autoanvil.enchantmentplants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

import java.util.List;

import static net.minecraft.enchantment.Enchantments.*;
import static net.minecraft.enchantment.Enchantments.AQUA_AFFINITY;

public class ShearUnbreaking extends IEnchantmentPlan {

    @Override
    public List<RegistryKey<Enchantment>> RequiredBooks() {
        return List.of(
            UNBREAKING
        );
    }

    @Override
    public List<Pair<Integer, Integer>> Plan() {
        return List.of(
            /* Step 0 */   new Pair<>(initialItemID(),   bookID(UNBREAKING))
        );
    }
}
