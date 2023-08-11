package io.github.jsbxyyx.tts;

public class Bytes {

    public static int indexOf(byte[] array, byte[] target) {
        if (array == null) throw new NullPointerException("array");
        if (target == null) throw new NullPointerException("target");
        if (target.length == 0) {
            return 0;
        }
        outer:
        for (int i = 0; i < array.length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

}
