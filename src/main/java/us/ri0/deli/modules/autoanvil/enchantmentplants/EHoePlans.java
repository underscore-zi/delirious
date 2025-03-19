package us.ri0.deli.modules.autoanvil.enchantmentplants;

public enum EHoePlans implements IHasPlan{
    FortuneTool {
        @Override
        public IEnchantmentPlan plan() {
            return new FortuneTool();
        }
    },
    SilkTouchTool {
        @Override
        public IEnchantmentPlan plan() {
            return new SilkTouchTool();
        }
    };
}

