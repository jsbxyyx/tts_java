package io.github.jsbxyyx.tts;

/**
 * @author jsbxyyx
 */
@FunctionalInterface
public interface TTSCallback {

    void call(byte[] bytes);

}
