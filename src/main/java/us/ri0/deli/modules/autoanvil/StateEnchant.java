package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.utils.player.SlotUtils;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import us.ri0.deli.modules.autoanvil.enchantmentplants.IEnchantmentPlan;

import java.util.concurrent.atomic.AtomicReference;

import static meteordevelopment.meteorclient.MeteorClient.mc;

class StateEnchant implements IState {
    private final ItemStack initialItem;
    private IEnchantmentPlan plan;
    private AtomicReference<ItemStack>[] results;
    private AnvilScreenHandler handler;
    private int stepIndex = 0;
    public StateEnchant(ItemStack initialItem, IEnchantmentPlan plan) {
        this.initialItem = initialItem;
        this.plan = plan;
        this.results = new AtomicReference[plan.Plan().size()];
        for(int i = 0; i < results.length; i++) {
            results[i] = new AtomicReference<>();
        }
    }

    @Override
    public IState next(Context ctx) throws RuntimeException{
        // If no screen is open, get the anvil back open
        if(mc.currentScreen == null) return new StateOpenAnvil(this);

        // Pausing because a screen is open, but it's not the anvil screen
        if(!(mc.currentScreen instanceof AnvilScreen)) {
            ctx.addDelay(10);
            return this;
        }

        this.handler = ((AnvilScreen) mc.currentScreen).getScreenHandler();

        var currentStep = plan.Step(stepIndex);
        // Finished enchanting, return to the start of the process
        if(currentStep == null) {
            var baseNext = new StateCloseScreen(new StateGathering());
            if(ctx.dropItemWhenComplete()) {
                var dropSlot = SlotUtils.indexToId(getItemSlot(-stepIndex));
                return new StateDropItem(dropSlot, baseNext);
            }
            return baseNext;
        }


        var leftSlot = getItemSlot(currentStep.getLeft());
        var rightSlot = getItemSlot(currentStep.getRight());

        stepIndex++;
        return new StateMoveToAnvilSlot(leftSlot, rightSlot,
                new StateTakeFromAnvil(results[stepIndex-1], this)
        );

    }

    public void restartLastStep() {
        if(stepIndex > 0) {
            stepIndex--;
        }
    }

    private int getItemSlot(Integer planSlotID) throws RuntimeException {
        if(planSlotID == 0) {
            // This should ONLY be used the first time the item is enchanted, otherwise use result ids
            var res = SearchUtils.findMatchingItem(initialItem);
            if(!res.found()) {
                throw new RuntimeException("Failed to find item for initial enchantment");
            }
            return res.slot();
        }
        else if(planSlotID < 0) {
            var newItem = results[-planSlotID-1].get();
            if(newItem.getCount() == 0) newItem.setCount(1);
            var res = SearchUtils.findMatchingItem(newItem);
            if(!res.found()) {
                throw new RuntimeException("Failed to find item for previous step");
            }
            return res.slot();
        } else /* planSlotID > 0  */ {
            var book = plan.RequiredBooks().get(planSlotID-1);
            var res = SearchUtils.findBook(book);
            if (!res.found()) {
                throw new RuntimeException("Failed to find book for previous step");
            }
            return res.slot();
        }
    }
}
