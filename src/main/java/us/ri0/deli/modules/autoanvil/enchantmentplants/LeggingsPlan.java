package us.ri0.deli.modules.autoanvil.enchantmentplants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

import java.util.List;

import static net.minecraft.enchantment.Enchantments.*;

public class LeggingsPlan extends IEnchantmentPlan {

    @Override
    public List<RegistryKey<Enchantment>> RequiredBooks() {
        return List.of(PROTECTION, UNBREAKING, MENDING);
    }

    @Override
    public List<Pair<Integer, Integer>> Plan() {
        return List.of(
            /* Step 0 */   new Pair<>(initialItemID(),    bookID(PROTECTION))
            /* Step 1 */ , new Pair<>(resultOfStep(0),    bookID(UNBREAKING))
            /* Step 2 */ , new Pair<>(resultOfStep(1),    bookID(MENDING))
        );
    }
}
