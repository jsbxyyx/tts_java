package io.github.jsbxyyx.tts;

import com.formdev.flatlaf.FlatLightLaf;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import net.java.dev.designgridlayout.DesignGridLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author jsbxyyx
 */
public class Main extends JFrame {

    private JTextArea ssmlPane;
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
    private File playFile;

    public Main() {
        setSize(850, 600);
        setTitle("TTS QQ:551304760");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridLayout(1, 0));


        ssmlPane = new JTextArea(10, 30);
        ssmlPane.setText(TtsClient.defaultSsml);
        ssmlPane.setLineWrap(true);
        ssmlPane.setWrapStyleWord(true);
        JScrollPane top = new JScrollPane(ssmlPane);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("SSML"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        logPane = new JTextArea(10, 30);
        logPane.setEditable(false);
        logPane.setLineWrap(true);
        logPane.setWrapStyleWord(true);
        JScrollPane bottom = new JScrollPane(logPane);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Log"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        leftPane.setResizeWeight(0.5);

        JPanel rightPane = new JPanel();
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

            layout.row().center().add(new JLabel("Language"));
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

            layout.row().center().add(new JLabel("Voice"));
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

            layout.row().center().add(new JLabel("Style"));
            layout.row().center().add(styleBox);
            layout.row().center().add(styleText);
            layout.emptyRow();

            rateSlider = new JSlider(-100, 200, 0);
            rateSlider.addChangeListener(e -> {
                rateText.setText(((JSlider) e.getSource()).getValue() + "");
            });
            rateText = new JTextField(columns);
            rateText.setText(rateSlider.getValue() + "");
            layout.row().center().add(new JLabel("Rate"));
            layout.row().center().add(rateSlider);
            layout.row().center().add(rateText);
            layout.emptyRow();

            pitchSlider = new JSlider(-50, 50, 0);
            pitchSlider.addChangeListener(e -> {
                pitchText.setText(((JSlider) e.getSource()).getValue() + "");
            });
            pitchText = new JTextField(columns);
            pitchText.setText(pitchSlider.getValue() + "");
            layout.row().center().add(new JLabel("Pitch"));
            layout.row().center().add(pitchSlider);
            layout.row().center().add(pitchText);
            layout.emptyRow();

            generateBtn = new JButton("Generate");
            generateBtn.addActionListener(e -> {
                try {
                    new TtsClient().setLog(logPane).createContent(ssmlPane.getText(), (f) -> {
                        playBtn.setEnabled(true);
                        playFile = f;
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            playBtn = new JButton("Play");
            playBtn.setEnabled(false);
            playBtn.addActionListener(e -> {
                new Thread(() -> {
                    try {
                        Player player = new Player(new FileInputStream(playFile));
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
                ssmlPane.setText(TtsClient.defaultSsml);
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
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            new Main().launch();
        });
    }

}
