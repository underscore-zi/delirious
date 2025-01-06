package us.ri0.deli.esp;

import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;

public class MockSetting<T> extends Setting<T> {
    public MockSetting(T defaultValue) {
        super("fake-setting", "", defaultValue, null, null, null);
    }

    @Override
    protected T parseImpl(String str) {
        return null;
    }

    @Override
    protected boolean isValueValid(T value) {
        return false;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        return null;
    }

    @Override
    protected T load(NbtCompound tag) {
        return null;
    }
}
