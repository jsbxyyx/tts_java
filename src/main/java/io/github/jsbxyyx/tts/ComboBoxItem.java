package io.github.jsbxyyx.tts;

import java.util.ArrayList;
import java.util.List;

public class ComboBoxItem {

    private static final List<ComboBoxItem> LANG_LIST = new ArrayList<>();
    private static final List<ComboBoxItem> VOICE_LIST = new ArrayList<>();
    private static final List<ComboBoxItem> STYLE_LIST = new ArrayList<>();

    static {
        LANG_LIST.add(new ComboBoxItem("zh-CN", "中文（普通话，简体）"));

        VOICE_LIST.add(new ComboBoxItem("XiaoxiaoNeural", "晓晓（女）"));
        VOICE_LIST.add(new ComboBoxItem("XiaochenNeural", "晓辰（女）"));
        VOICE_LIST.add(new ComboBoxItem("YunxiNeural", "云希（男）"));
        VOICE_LIST.add(new ComboBoxItem("sichuan-YunxiNeural", "云希（男）四川话"));

        STYLE_LIST.add(new ComboBoxItem("general", "普通"));
    }

    public static List<ComboBoxItem> getLangList() {
        return new ArrayList<>(LANG_LIST);
    }

    public static List<ComboBoxItem> getVoiceList() {
        return new ArrayList<>(VOICE_LIST);
    }

    public static List<ComboBoxItem> getStyleList() {
        return new ArrayList<>(STYLE_LIST);
    }

    private final String key;
    private final String value;

    public ComboBoxItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
