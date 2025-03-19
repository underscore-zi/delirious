package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.utils.player.FindItemResult;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

class StateGathering implements IState {
    // Since updating inventory can be kinda slow, only do this once and then we can just track the delta
    private boolean inventoriesUpdated;

    // Just so we don't run into a loop of trying and failing to fetch from storage over and over
    private boolean justFetchedFromStorage = false;
    public StateGathering() {
        this.justFetchedFromStorage = false;
    }
    @Override public IState next(Context ctx) {

        // Best case is we have an item and books in our inventory and can continue on
        for(Item item : ctx.enchantableItems()) {
            var res = findItemInPersonalInventory(item);
            if(!res.found()) continue;
            var plan = ctx.planForItem(item);
            var books = plan.RequiredBooks();
            if(!SearchUtils.hasBooks(books)) continue;

            justFetchedFromStorage = false;
            return new StateOpenAnvil(new StateEnchant(mc.player.getInventory().getStack(res.slot()), plan));
        }

        if (justFetchedFromStorage) {
            return new StateError("Failed to move items into personal inventory.");
        }

        // Okay nothing in our inventory to enchant, update our storage cache
        if(!inventoriesUpdated) {
            inventoriesUpdated = true;
            return new StateUpdateContainerInventories(this);
        }

        justFetchedFromStorage = true;
        // Now iterate all the enabled items and see if we have all the books
        for(Item item : ctx.enchantableItems()) {
            HashMap<BlockPos, List<Integer>> movements = new HashMap<>();
            var resItem = ctx.findInInventories(item.getDefaultStack());
            if(!resItem.getLeft()) continue;
            var plan = ctx.planForItem(item);

            var bookNotFound = false;
            for(var enchant : plan.RequiredBooks()) {
                var resBook = ctx.findInInventories(List.of(enchant));
                if(!resBook.getLeft()) {
                    bookNotFound = true;
                    break;
                }

                // If it is null then it is already in our inventory
                if(resBook.getRight() == null) continue;

                // Otherwise add this to the set of movements
                var pos = resBook.getRight().getLeft();
                var index = resBook.getRight().getRight();
                if(!movements.containsKey(pos)) movements.put(pos, new LinkedList<Integer>());
                movements.get(pos).add(index);
            }
            if(bookNotFound) continue;

            // Add the item to the movements
            if(resItem.getRight() != null) {
                var pos = resItem.getRight().getLeft();
                var index = resItem.getRight().getRight();
                if(!movements.containsKey(pos)) movements.put(pos, new LinkedList<Integer>());
                movements.get(pos).add(index);
            }
            // Now do the movements, and come back here, we should fall down the first path
            return new StateDoContainerMovements(movements, this);
        }

        return new StateError("Nothing to enchant or missing books.");
    }

    private FindItemResult findItemInPersonalInventory(Item item) {
        return SearchUtils.findMatchingItem(item.getDefaultStack());
    }

}
