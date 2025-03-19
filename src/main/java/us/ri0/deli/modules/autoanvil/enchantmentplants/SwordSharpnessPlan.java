package us.ri0.deli.modules.autoanvil.enchantmentplants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

import java.util.List;

import static net.minecraft.enchantment.Enchantments.*;

public class SwordSharpnessPlan extends IEnchantmentPlan {

    @Override
    public List<RegistryKey<Enchantment>> RequiredBooks() {
        return List.of(
            SHARPNESS, FIRE_ASPECT, KNOCKBACK, LOOTING, MENDING, SWEEPING_EDGE, UNBREAKING
        );
    }

    @Override
    public List<Pair<Integer, Integer>> Plan() {
        return List.of(
            /* Step 0 */   new Pair<>(initialItemID(),       bookID(LOOTING))
            /* Step 1 */ , new Pair<>(bookID(SWEEPING_EDGE), bookID(UNBREAKING))
            /* Step 2 */ , new Pair<>(resultOfStep(0),       resultOfStep(1))
            /* Step 3 */ , new Pair<>(bookID(SHARPNESS),     bookID(MENDING))
            /* Step 4 */ , new Pair<>(resultOfStep(2),       resultOfStep(3))
            /* Step 5 */ , new Pair<>(bookID(FIRE_ASPECT),   bookID(KNOCKBACK))
            /* Step 6 */ , new Pair<>(resultOfStep(4),       resultOfStep(5))
        );
    }
}
