package us.ri0.deli.modules.autoanvil.enchantmentplants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

import java.util.List;

public abstract class IEnchantmentPlan {
    public abstract List<RegistryKey<Enchantment>> RequiredBooks();
    public abstract List<Pair<Integer, Integer>> Plan();

    /**
     * Returns the items to be placed in the anvil
     */
    public Pair<Integer, Integer> Step(int step) {
        if(step < 0 || step >= Plan().size()) return null;
        return Plan().get(step);
    }
    /*
     * The Internal ID system is just negative numbers are the index-1 of a result in the plan
     * A 0 is the item to be enchanted
     * and positive numbers are index+1 of the book to be used
     */

    /**
     * Returns the internal ID representing the item to be enchanted
     */
    public Integer initialItemID() {
        return 0;
    }

    /**
     * Returns the internal ID representing the result of the given step of the plan
     * @param step
     */
    public Integer resultOfStep(int step) {
        return -step-1;
    }

    /**
     * Returns the internal ID representing the book with the given enchantment
     * @param book
     */
    public Integer bookID(RegistryKey<Enchantment> book) {
        return RequiredBooks().indexOf(book) +1;
    }
}
