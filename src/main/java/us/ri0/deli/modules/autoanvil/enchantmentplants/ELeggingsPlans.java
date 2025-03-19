package us.ri0.deli.modules.autoanvil.enchantmentplants;

public enum ELeggingsPlans implements IHasPlan {
    LeggingsPlan {
        @Override
        public IEnchantmentPlan plan() {
            return new LeggingsPlan();
        }
    },
    LeggingsBlastPlan {
        @Override
        public IEnchantmentPlan plan() {
            return new LeggingsBlastPlan();
        }
    },
}
