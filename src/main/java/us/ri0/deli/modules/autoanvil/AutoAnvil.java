package us.ri0.deli.modules.autoanvil;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import us.ri0.deli.Addon;
import us.ri0.deli.modules.autoanvil.enchantmentplants.*;

public class AutoAnvil extends Module {
    public static final String PREFIX = "AutoAnvil";
    public AutoAnvil() {
        super(Addon.CATEGORY, "auto-anvil", "Enchants your gear");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final SettingGroup sgToolEnables = settings.createGroup("Enable Tools");
    private final SettingGroup sgWeaponEnables = settings.createGroup("Enable Weapons");
    private final SettingGroup sgArmorEnables = settings.createGroup("Enable Armor");

    private final SettingGroup sgToolEnchantmentPlans = settings.createGroup("Enchantment Plans - Tools");
    private final SettingGroup sgWeaponEnchantmentPlans = settings.createGroup("Enchantment Plans - Weapons");
    private final SettingGroup sgArmorEnchantmentPlans = settings.createGroup("Enchantment Plans - Armor");

    public final Setting<Integer> reach = sgGeneral.add(new IntSetting.Builder()
        .name("reach-distance")
        .description("Maximum distance to reach")
        .defaultValue(5)
        .min(1)
        .sliderMax(10)
        .build()
    );
    public final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("tick-delay")
        .description("Delay in ticks between actions. Going too fast can lead to anticheat and desync issues.")
        .defaultValue(3)
        .min(1)
        .sliderMax(20)
        .build()
    );
    public final Setting<Boolean> useKillAura = sgGeneral.add(new BoolSetting.Builder()
        .name("use-kill-aura")
        .description("Use Kill Aura when XP is needed")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> dropItemWhenComplete = sgGeneral.add(new BoolSetting.Builder()
        .name("drop-item")
        .description("Drop the item at your feet when fully enchanted")
        .defaultValue(false)
        .build()
    );
    public final Setting<Boolean> useChests = sgGeneral.add(new BoolSetting.Builder()
        .name("use-chests")
        .description("Look for necessary items in chests")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> useShulkers = sgGeneral.add(new BoolSetting.Builder()
        .name("use-shulkers")
        .description("Look for necessary items in shulkers")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> enchantDiamond = sgGeneral.add(new BoolSetting.Builder()
        .name("enchant-diamond")
        .description("Enchant diamond items")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> enchantNetherite = sgGeneral.add(new BoolSetting.Builder()
        .name("enchant-netherite")
        .description("Enchant netherite items")
        .defaultValue(false)
        .build()
    );

    /** Tools **/

    // pickaxe
    public final Setting<Boolean> enchantPickaxes = sgToolEnables.add(new BoolSetting.Builder().name("enchant-pickaxes").description("Enchant pickaxes").defaultValue(false).build());
    public final Setting<EPickaxePlans> pickaxePlan = sgToolEnchantmentPlans.add(new EnumSetting.Builder<EPickaxePlans>().name("pickaxe-plan").description("The enchantment plans to use for pickaxes").defaultValue(EPickaxePlans.FortuneTool).visible(enchantPickaxes::get).build());

    // axe
    public final Setting<Boolean> enchantAxes = sgToolEnables.add(new BoolSetting.Builder().name("enchant-axes").description("Enchant axes").defaultValue(false).build());
    public final Setting<EAxePlans> axePlan = sgToolEnchantmentPlans.add(new EnumSetting.Builder<EAxePlans>().name("axe-plan").description("The enchantment plans to use for axes").defaultValue(EAxePlans.FortuneTool).visible(enchantAxes::get).build());

    // shovel
    public final Setting<Boolean> enchantShovels = sgToolEnables.add(new BoolSetting.Builder().name("enchant-shovels").description("Enchant shovels").defaultValue(false).build());
    public final Setting<EShovelPLans> shovelPlan = sgToolEnchantmentPlans.add(new EnumSetting.Builder<EShovelPLans>().name("shovel-plan").description("The enchantment plans to use for shovels").defaultValue(EShovelPLans.FortuneTool).visible(enchantShovels::get).build());

    // hoe
    public final Setting<Boolean> enchantHoes = sgToolEnables.add(new BoolSetting.Builder().name("enchant-hoes").description("Enchant hoes").defaultValue(false).build());
    public final Setting<EHoePlans> hoePlan = sgToolEnchantmentPlans.add(new EnumSetting.Builder<EHoePlans>().name("hoe-plan").description("The enchantment plans to use for hoes").defaultValue(EHoePlans.FortuneTool.FortuneTool).visible(enchantHoes::get).build());

    /** Weapons **/

    // sword
    public final Setting<Boolean> enchantSwords = sgWeaponEnables.add(new BoolSetting.Builder().name("enchant-swords").description("Enchant swords").defaultValue(false).build());
    public final Setting<ESwordPlans> swordPlan = sgWeaponEnchantmentPlans.add(new EnumSetting.Builder<ESwordPlans>().name("sword-plan").description("The enchantment plans to use for swords").defaultValue(ESwordPlans.SwordSharpnessPlan).visible(enchantSwords::get).build());

    /** Armor **/

    // helmet
    public final Setting<Boolean> enchantHelmets = sgArmorEnables.add(new BoolSetting.Builder().name("enchant-helmets").description("Enchant helmets").defaultValue(false).build());
    public final Setting<EHelmetPlans> helmetPlan = sgArmorEnchantmentPlans.add(new EnumSetting.Builder<EHelmetPlans>().name("helmet-plan").description("The enchantment plans to use for helmets").defaultValue(EHelmetPlans.HelmetPlan).visible(enchantHelmets::get).build());

    // chestplate
    public final Setting<Boolean> enchantChestplates = sgArmorEnables.add(new BoolSetting.Builder().name("enchant-chestplates").description("Enchant chestplates").defaultValue(false).build());
    public final Setting<EChestplatePlans> chestplatePlan = sgArmorEnchantmentPlans.add(new EnumSetting.Builder<EChestplatePlans>().name("chestplate-plan").description("The enchantment plans to use for chestplates").defaultValue(EChestplatePlans.ChestplatePlan).visible(enchantChestplates::get).build());

    // leggings
    public final Setting<Boolean> enchantLeggings = sgArmorEnables.add(new BoolSetting.Builder().name("enchant-leggings").description("Enchant leggings").defaultValue(false).build());
    public final Setting<ELeggingsPlans> leggingsPlan = sgArmorEnchantmentPlans.add(new EnumSetting.Builder<ELeggingsPlans>().name("leggings-plan").description("The enchantment plans to use for leggings").defaultValue(ELeggingsPlans.LeggingsBlastPlan).visible(enchantLeggings::get).build());

    // boots
    public final Setting<Boolean> enchantBoots = sgArmorEnables.add(new BoolSetting.Builder().name("enchant-boots").description("Enchant boots").defaultValue(false).build());
    public final Setting<EBootsPlans> bootsPlan = sgArmorEnchantmentPlans.add(new EnumSetting.Builder<EBootsPlans>().name("boots-plan").description("The enchantment plans to use for boots").defaultValue(EBootsPlans.BootsPlan).visible(enchantBoots::get).build());

    private IState state;
    private Context context;

    @Override
    public void onActivate() {
        context = new Context(this);
        state = new StateWaitForScreen(null, new StateGathering());
    }

    @Override
    public void onDeactivate() {
        context = null;
        state = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(context.tickDelay > 0) {
            context.tickDelay--;
            return;
        }
        try {
            state = state.next(context);
            if(delay.get() > 0) {
                context.addDelay(delay.get());
            }
        } catch(RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            state = new StateError(e.getMessage());
        }
        if(state == null) {
            this.toggle();
        }
    }

    @Override
    public String getInfoString() {
        if(state == null) return "";
        var name =  state.getClass().getSimpleName();
        if(name.startsWith("State")) {
            name = name.substring(5);
        }
        return name;
    }

}
