package io.github.jsbxyyx.tts;

/**
 * @author jsbxyyx
 */
@FunctionalInterface
public interface TtsCallback {

    void call(byte[] bytes);

}
