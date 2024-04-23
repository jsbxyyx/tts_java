package io.github.jsbxyyx.tts;

import javax.swing.*;
import java.io.IOException;

public class JTextAreaAdapter implements Appendable {

    private JTextArea textArea;

    public JTextAreaAdapter(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        textArea.append(csq.toString());
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        textArea.append(csq.subSequence(start, end).toString());
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        textArea.append(String.valueOf(c));
        return this;
    }

}
