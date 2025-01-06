package us.ri0.deli.esp;

import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class EspOptions {
    public Setting<Boolean> tracer;
    public Setting<SettingColor> tracerColor;
    public Setting<SettingColor> lineColor;
    public Setting<SettingColor> sideColor;
    public Setting<ShapeMode> mode;
    public Setting<Integer> renderDistance;
    public int excludeDir = 0;

    public EspOptions() {
        tracer = new MockSetting<Boolean>(true);
        tracerColor = new MockSetting<SettingColor>(new SettingColor(255, 0, 0));
        lineColor = new MockSetting<SettingColor>(new SettingColor(255, 0, 0));
        sideColor = new MockSetting<SettingColor>(new SettingColor(255, 0, 0, 50));
        mode = new MockSetting<ShapeMode>(ShapeMode.Both);
        renderDistance = new MockSetting<Integer>(15);
    }
}
