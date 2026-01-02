package us.ri0.deli;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import us.ri0.deli.hud.EntityCounter;
import us.ri0.deli.modules.*;
import us.ri0.deli.modules.autoanvil.AutoAnvil;
import us.ri0.deli.modules.caveair.MissingCaveAir;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Delirious");
    public static final HudGroup HUD_GROUP = new HudGroup("Delirious");


    @Override
    public void onInitialize() {
        LOG.info("Initializing Delirious Addon");
        Modules.get().add(new MissingCaveAir());
        Modules.get().add(new AltElytraSources());
        Modules.get().add(new MinecartToucher());
        Modules.get().add(new StackedStorage());
        Modules.get().add(new DisplacedStack());
        Modules.get().add(new AreaLoader());
        //Modules.get().add(new AutoEnchant());
        //Modules.get().add(new PortalBuilder());
        Modules.get().add(new AutoAnvil());
        //Modules.get().add(new ChatPoC());
        Modules.get().add(new BonemealFarmer());
        Modules.get().add(new ElytraReplace());
        Modules.get().add(new RocketCrafter());
        Modules.get().add(new SpawnTracker());

        Modules.get().add(new WaypointFollower());
        Hud.get().register(EntityCounter.INFO);
        Modules.get().add(new BetterShulkerViewer());
        Modules.get().add(new GrimScaffold());
        Modules.get().add(new ItemFrameESP());
        Modules.get().add(new InstaAura());
        Modules.get().add(new TeleportModuleControl());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "us.ri0.deli";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("underscore-zi", "delirious");
    }

}
