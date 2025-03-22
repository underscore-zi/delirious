package us.ri0.deli.modules.autoanvil.enchantmentplants;

public enum EShearsPlans implements IHasPlan {
    UnbreakableShears {
        @Override
        public IEnchantmentPlan plan() {
            return new ShearUnbreaking();
        }
    },

    FullEnchantedShears {
        @Override
        public IEnchantmentPlan plan() {
            return new ShearsFullEnchantPlan();
        }
    };
}
