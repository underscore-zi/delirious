package us.ri0.deli.modules.autoanvil.enchantmentplants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

import java.util.List;

import static net.minecraft.enchantment.Enchantments.*;

public class HelmetPlan  extends IEnchantmentPlan {

    @Override
    public List<RegistryKey<Enchantment>> RequiredBooks() {
        return List.of(
            PROTECTION ,AQUA_AFFINITY ,MENDING, RESPIRATION ,UNBREAKING
        );
    }

    @Override
    public List<Pair<Integer, Integer>> Plan() {
        return List.of(
            /* Step 0 */   new Pair<>(initialItemID(),    bookID(RESPIRATION))
            /* Step 1 */ , new Pair<>(bookID(PROTECTION), bookID(MENDING))
            /* Step 2 */ , new Pair<>(resultOfStep(0),    resultOfStep(1))
            /* Step 3 */ , new Pair<>(bookID(UNBREAKING), bookID(AQUA_AFFINITY))
            /* Step 4 */ , new Pair<>(resultOfStep(2),    resultOfStep(3))
        );
    }
}
