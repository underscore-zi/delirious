package us.ri0.deli.modules.autoanvil.enchantmentplants;

public enum EHelmetPlans implements IHasPlan {
    HelmetPlan {
        @Override
        public IEnchantmentPlan plan() {
            return new HelmetPlan();
        }
    },
}
