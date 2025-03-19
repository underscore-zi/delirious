package us.ri0.deli.modules.autoanvil.enchantmentplants;

public enum EBootsPlans implements IHasPlan {
    BootsPlan {
        @Override
        public IEnchantmentPlan plan() {
            return new BootsPlan();
        }
    },
}
