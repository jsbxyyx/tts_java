package io.github.jsbxyyx.tts;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    private JTextArea ssmlPane;
    private JTextArea logPane;
    private JButton playBtn;

    public Main() {
        setSize(600, 400);
        setTitle("TTS QQ:551304760");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridLayout(1, 0));


        ssmlPane = new JTextArea(10, 30);
        ssmlPane.setText("<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\"en-US\">\n" +
                "<voice name=\"zh-CN-XiaoxiaoNeural\">\n" +
                "<prosody rate=\"0%\" pitch=\"0%\">\n" +
                "Java编程思想 第4版\n" +
                "</prosody>\n" +
                "</voice>\n" +
                "</speak>");
        ssmlPane.setLineWrap(true);
        ssmlPane.setWrapStyleWord(true);
        JScrollPane top = new JScrollPane(ssmlPane);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("ssml"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        logPane = new JTextArea(10, 30);
        logPane.setEditable(false);
        logPane.setLineWrap(true);
        logPane.setWrapStyleWord(true);
        JScrollPane bottom = new JScrollPane(logPane);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("log"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        leftPane.setResizeWeight(0.5);

        JPanel rightPane = new JPanel();

        playBtn = new JButton("Play");
        playBtn.addActionListener(e -> {
            try {
                new TtsClient().setLog(logPane).createContent(ssmlPane.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        rightPane.add(playBtn);


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
        splitPane.setOneTouchExpandable(true);
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
