package us.ri0.deli.modules.autoanvil.enchantmentplants;

public enum EChestplatePlans implements IHasPlan {
    ChestplatePlan {
        @Override
        public IEnchantmentPlan plan() {
            return new ChestplatePlan();
        }
    },
}
