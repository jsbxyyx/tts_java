package io.github.jsbxyyx.tts;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ComboBoxItem {

    private static final List<ComboBoxItem> LANG_LIST = new ArrayList<>();
    private static final List<ComboBoxItem> VOICE_LIST = new ArrayList<>();
    private static final List<ComboBoxItem> STYLE_LIST = new ArrayList<>();

    private static final Properties prop = new Properties();

    static {
        load();

        List<String> langs = getPropertyList("lang");
        for (String value : langs) {
            String[] split = value.split("\\,");
            LANG_LIST.add(new ComboBoxItem(split[0].trim(), split[1].trim()));
        }

        List<String> voices = getPropertyList("voice");
        for (String value : voices) {
            String[] split = value.split("\\,");
            VOICE_LIST.add(new ComboBoxItem(split[0].trim(), split[1].trim()));
        }

        List<String> styles = getPropertyList("style");
        for (String value : styles) {
            String[] split = value.split("\\,");
            STYLE_LIST.add(new ComboBoxItem(split[0].trim(), split[1].trim()));
        }
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

    private static void load() {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            prop.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getPropertyList(String key) {
        List<String> result = new LinkedList<>();
        String value;
        for (int i = 0; (value = prop.getProperty(key + "." + i)) != null; i++) {
            result.add(value);
        }
        return result;
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
