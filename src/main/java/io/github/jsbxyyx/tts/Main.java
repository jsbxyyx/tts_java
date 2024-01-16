package io.github.jsbxyyx.tts;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import net.java.dev.designgridlayout.DesignGridLayout;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author jsbxyyx
 */
public class Main extends JFrame {

    private static final String base_dir = System.getProperty("tts.output.dir", System.getProperty("user.dir"));

    private JTabbedPane tabbedPane;
    private JTextArea ssmlPane;
    private JTextArea textPane;
    private JTextArea logPane;

    private JComboBox langBox;
    private JTextField langText;

    private JComboBox voiceBox;
    private JTextField voiceText;

    private JComboBox styleBox;
    private JTextField styleText;

    private JSlider rateSlider;
    private JTextField rateText;

    private JSlider pitchSlider;
    private JTextField pitchText;

    private JButton generateBtn;
    private JButton playBtn;
    private JButton resetBtn;

    int columns = 15;
    private byte[] playFile;

    public Main() {
        setSize(850, 600);
        setTitle("TTS QQ Group: 551304760");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridLayout(1, 0));

        textPane = new JTextArea(10, 30);
        textPane.setText("你可将此文本替换为所需的任何文本。");
        textPane.setLineWrap(true);
        textPane.setWrapStyleWord(true);
        JScrollPane top1 = new JScrollPane(textPane);
        top1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("TEXT"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        ssmlPane = new JTextArea(10, 30);
        ssmlPane.setText(TTSClient2.defaultSsml);
        ssmlPane.setLineWrap(true);
        ssmlPane.setWrapStyleWord(true);
        JScrollPane top2 = new JScrollPane(ssmlPane);
        top2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("SSML"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        tabbedPane = new JTabbedPane();
        tabbedPane.add("text", top1);
        tabbedPane.add("ssml", top2);

        logPane = new JTextArea(10, 30);
        logPane.setEditable(false);
        logPane.setLineWrap(true);
        logPane.setWrapStyleWord(true);
        JScrollPane bottom = new JScrollPane(logPane);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Log"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, bottom);
        leftPane.setResizeWeight(0.5);

        JPanel rightPane = new JPanel();
        rightPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Assist"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        {
            DesignGridLayout layout = new DesignGridLayout(rightPane);

            langBox = new JComboBox();
            List<ComboBoxItem> langList = ComboBoxItem.getLangList();
            for (ComboBoxItem item : langList) {
                langBox.addItem(item);
            }
            langBox.addItemListener(e -> {
                ComboBoxItem item = (ComboBoxItem) e.getItem();
                langText.setText(item.getKey());
            });
            langText = new JTextField();
            langText.setText(((ComboBoxItem) langBox.getSelectedItem()).getKey());

            layout.row().center().add(new JLabel("Language(语言)"));
            layout.row().center().add(langBox);
            layout.row().center().add(langText);
            layout.emptyRow();

            voiceBox = new JComboBox();
            List<ComboBoxItem> voiceList = ComboBoxItem.getVoiceList();
            for (ComboBoxItem item : voiceList) {
                voiceBox.addItem(item);
            }
            voiceBox.addItemListener(e -> {
                ComboBoxItem item = (ComboBoxItem) e.getItem();
                voiceText.setText(item.getKey());
            });
            voiceText = new JTextField(columns);
            voiceText.setText(((ComboBoxItem) voiceBox.getSelectedItem()).getKey());

            layout.row().center().add(new JLabel("Voice(语音)"));
            layout.row().center().add(voiceBox);
            layout.row().center().add(voiceText);
            layout.emptyRow();

            styleBox = new JComboBox();
            List<ComboBoxItem> styleList = ComboBoxItem.getStyleList();
            for (ComboBoxItem item : styleList) {
                styleBox.addItem(item);
            }
            styleBox.addItemListener(e -> {
                ComboBoxItem item = (ComboBoxItem) e.getItem();
                styleText.setText(item.getKey());
            });
            styleText = new JTextField(columns);
            styleText.setText(((ComboBoxItem) styleBox.getSelectedItem()).getKey());

            layout.row().center().add(new JLabel("Style(风格)"));
            layout.row().center().add(styleBox);
            layout.row().center().add(styleText);
            layout.emptyRow();

            rateSlider = new JSlider(-100, 200, 0);
            rateSlider.addChangeListener(e -> {
                rateText.setText(((JSlider) e.getSource()).getValue() + "");
            });
            rateText = new JTextField(columns);
            rateText.setText(rateSlider.getValue() + "");
            layout.row().center().add(new JLabel("Rate(语速)"));
            layout.row().center().add(rateSlider);
            layout.row().center().add(rateText);
            layout.emptyRow();

            pitchSlider = new JSlider(-50, 50, 0);
            pitchSlider.addChangeListener(e -> {
                pitchText.setText(((JSlider) e.getSource()).getValue() + "");
            });
            pitchText = new JTextField(columns);
            pitchText.setText(pitchSlider.getValue() + "");
            layout.row().center().add(new JLabel("Pitch(音调)"));
            layout.row().center().add(pitchSlider);
            layout.row().center().add(pitchText);
            layout.emptyRow();

            generateBtn = new JButton("Generate");
            generateBtn.addActionListener(e -> {
                try {
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    ByteArrayOutputStream output = null;
                    if (selectedIndex == 0) {
                        output = TTSClient2.audioByText(textPane.getText(),
                                "zh-CN", "zh-CN-YunxiNeural", "+0%", "+1000%");
                    } else {
                        output = TTSClient2.audioBySsml(ssmlPane.getText());
                    }

                    if (output.size() > 0) {
                        String name = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                        File file = new File(base_dir + "/tts-" + name + ".mp3");
                        logPane.append("write file ::: " + file.getAbsolutePath() + "\n");
                        try (FileOutputStream out = new FileOutputStream(file)) {
                            output.writeTo(out);
                        }
                        playBtn.setEnabled(true);
                        playFile = output.toByteArray();
                    } else {
                        logPane.append("generate audio failed.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            playBtn = new JButton("Play");
            playBtn.setEnabled(false);
            playBtn.addActionListener(e -> {
                new Thread(() -> {
                    try (ByteArrayInputStream input = new ByteArrayInputStream(playFile)) {
                        Player player = new Player(input);
                        player.play();
                    } catch (JavaLayerException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            });

            resetBtn = new JButton("Reset");
            resetBtn.addActionListener(e -> {
                ssmlPane.setText(TTSClient1.defaultSsml);
                logPane.setText("");
            });

            layout.row().center().add(generateBtn);
            layout.row().center().add(playBtn);
            layout.row().center().add(resetBtn);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
        // splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.9);
        add(splitPane);
    }

    public void launch() {
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
//        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            new Main().launch();
        });
    }

}
