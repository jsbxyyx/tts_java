package io.github.jsbxyyx.tts;

import java.io.File;

/**
 * @author jsbxyyx
 */
@FunctionalInterface
public interface TtsCallback {

    void call(File file);

}
