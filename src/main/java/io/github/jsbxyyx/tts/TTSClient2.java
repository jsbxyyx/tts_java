package io.github.jsbxyyx.tts;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TTSClient2 extends WebSocketClient {

    public static String defaultSsml = "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xmlns:mstts='https://www.w3.org/2001/mstts' xml:lang='zh-CN'>\r\n" +
            "<voice name='zh-CN-XiaoxiaoNeural'>\r\n" +
            "<prosody pitch='+0Hz' rate='+0%' volume='+0%'>\n" +
            "你可将此文本替换为所需的任何文本。\n" +
            "</prosody>\n" +
            "</voice>\n" +
            "</speak>";
    private ByteArrayOutputStream buffers = new ByteArrayOutputStream(1024);
    private CompletableFuture<ByteArrayOutputStream> cf;

    private static Appendable appendable;

    public TTSClient2(URI serverUri, Map<String, String> httpHeaders, CompletableFuture<ByteArrayOutputStream> cf) {
        // wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?TrustedClientToken=6A5AA1D4EAFF4E9FB37E23D68491D6F4
        // https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/voices/list?trustedclienttoken=6A5AA1D4EAFF4E9FB37E23D68491D6F4
        super(serverUri, httpHeaders);
        this.cf = cf;
    }

    public static void setAppendable(Appendable appendable) {
        TTSClient2.appendable = appendable;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        String CONFIG_PATTERN = "X-Timestamp:%s\r\n" +
                "Content-Type:application/json; charset=utf-8\r\n" +
                "Path:speech.config\r\n" +
                "\r\n" +
                "{\"context\":{\"synthesis\":{\"audio\":{\"metadataoptions\":{\"sentenceBoundaryEnabled\":\"false\",\"wordBoundaryEnabled\":\"true\"},\"outputFormat\":\"%s\"}}}}";
        send(String.format(
                CONFIG_PATTERN,
                date(),
                "audio-24khz-48kbitrate-mono-mp3")
        );
    }

    @Override
    public void onMessage(String text) {
        log("onMessage:\r\n" + text);
        if (text.contains("turn.start")) {
            // （新的）音频流开始传输开始，清空重置buffer
            buffers.reset();
        } else if (text.contains("turn.end")) {
            // 音频流结束，写为文件
            try {
                cf.complete(buffers);
                close(1000);
            } catch (Exception e) {
                log(e.getMessage());
            }
        }
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        // Content-Type:audio
        // Path:audio\r\n
        byte[] rawData = bytes.array();
        byte[] sep = "Path:audio\r\n".getBytes(StandardCharsets.UTF_8);
        int index = indexOf(rawData, sep);
        byte[] data = new byte[rawData.length - (index + sep.length)];
        System.arraycopy(rawData, index + sep.length, data, 0, data.length);
        try {
            buffers.write(data);
        } catch (IOException ignore) {
        }
        log(encodeHex(rawData));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log("onClose code:" + code + ", reason:" + reason + ", remote:" + remote);
        if (code != 1000) {
            cf.complete(new ByteArrayOutputStream());
        }
    }

    @Override
    public void onError(Exception e) {
        cf.complete(new ByteArrayOutputStream());
        log("onClose e:" + e.getMessage());
    }

    static void log(String str) {
        System.out.println(str);
        if (appendable != null) {
            try {
                appendable.append(str).append("\n");
            } catch (IOException ignore) {
            }
        }
    }

    static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    static String date() {
        return new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z").format(new Date());
    }

    static int indexOf(byte[] array, byte[] target) {
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

    static String encodeHex(final byte[] data) {
        char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        final int dataLength = data.length;
        final char[] out = new char[dataLength << 1];
        for (int i = 0, j = 0; i < dataLength; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }

    public static ByteArrayOutputStream audioBySsml(String ssml) throws Exception {
        ssml = String.format("X-RequestId:%s\r\n" +
                "Content-Type:application/ssml+xml\r\n" +
                "X-Timestamp:%sZ\r\n" +
                "Path:ssml\r\n" +
                "\r\n%s", uuid(), date(), ssml);
        log("ssml:[" + ssml + "]");
        CompletableFuture<ByteArrayOutputStream> cf = new CompletableFuture<>();
        // Microsoft Server Speech Text to Speech Voice (en-US, AriaNeural)
        // zh-CN-XiaoxiaoNeural
        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.44");
        httpHeaders.put("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold");
        TTSClient2 client2 = new TTSClient2(new URI(
                "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1" +
                        "?TrustedClientToken=6A5AA1D4EAFF4E9FB37E23D68491D6F4" +
                        "&Retry-After=200&ConnectionId=" + uuid()
        ), httpHeaders, cf);
        client2.connect();
        while (!client2.isOpen()) {
            TimeUnit.MILLISECONDS.sleep(50);
            if (client2.isClosed()) {
                log("client is closed.");
                return new ByteArrayOutputStream();
            }
        }
        client2.send(ssml);
        ByteArrayOutputStream output = cf.get(30, TimeUnit.SECONDS);
        return output;
    }

    public static ByteArrayOutputStream audioByText(String text, String lang, String voiceName, String rate, String volume) throws Exception {
        if (lang == null) lang = "zh-CN";
        if (voiceName == null) voiceName = "zh-CN-XiaoxiaoNeural";
        if (rate == null) rate = "+0%";
        if (volume == null) volume = "+0%";
        final String SPEAK_PATTERN =
                "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xmlns:mstts='https://www.w3.org/2001/mstts' xml:lang='%s'>\r\n" +
                "<voice name='%s'>\r\n" +
                "<prosody pitch='+0Hz' rate='%s' volume='%s'>" +
                "%s" +
                "</prosody>" +
                "</voice>" +
                "</speak>";
        String ssml = String.format(
                SPEAK_PATTERN,
                lang,
                voiceName,
                rate,
                volume,
                text
        );
        return audioBySsml(ssml);
    }

    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream output = audioByText("你好", null, null, null, null);
        String name = System.currentTimeMillis() + ".mp3";
        Files.write(new File(
                System.getProperty("user.dir") + "/" + name
        ).toPath(), output.toByteArray());
        log("write " + name);
    }

}
