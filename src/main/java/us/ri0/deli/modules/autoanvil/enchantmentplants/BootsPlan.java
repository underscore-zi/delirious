package us.ri0.deli.modules.autoanvil.enchantmentplants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

import java.util.List;

import static net.minecraft.enchantment.Enchantments.*;

public class BootsPlan extends IEnchantmentPlan {
    @Override
    public List<RegistryKey<Enchantment>> RequiredBooks() {
        return List.of(
            DEPTH_STRIDER, UNBREAKING, FEATHER_FALLING, PROTECTION, MENDING
        );
    }

    @Override
    public List<Pair<Integer, Integer>> Plan() {
        return List.of(
            /* Step 0 */   new Pair<>(initialItemID(),         bookID(DEPTH_STRIDER))
            /* Step 1 */ , new Pair<>(bookID(FEATHER_FALLING), bookID(UNBREAKING))
            /* Step 2 */ , new Pair<>(resultOfStep(0),         resultOfStep(1))
            /* Step 3 */ , new Pair<>(bookID(PROTECTION),      bookID(MENDING))
            /* Step 4 */ , new Pair<>(resultOfStep(2),         resultOfStep(3))
        );
    }
}
