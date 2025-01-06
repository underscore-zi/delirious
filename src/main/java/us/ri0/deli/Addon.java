package us.ri0.deli;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import us.ri0.deli.modules.*;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Delirious");


    @Override
    public void onInitialize() {
        LOG.info("Initializing Delirious Addon");
        Modules.get().add(new MissingCaveAir());
        Modules.get().add(new AltElytraSources());
        Modules.get().add(new MinecartToucher());
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
