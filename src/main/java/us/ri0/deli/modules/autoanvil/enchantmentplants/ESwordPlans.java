package us.ri0.deli.modules.autoanvil.enchantmentplants;

public enum ESwordPlans implements IHasPlan {
    SwordSharpnessPlan {
        @Override
        public IEnchantmentPlan plan() {
            return new SwordSharpnessPlan();
        }
    },
}
