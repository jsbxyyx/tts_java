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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TTSClient2 extends WebSocketClient {

    private ByteArrayOutputStream buffers = new ByteArrayOutputStream(1024);
    private CompletableFuture<ByteArrayOutputStream> cf;

    public TTSClient2(URI serverUri, Map<String, String> httpHeaders, CompletableFuture<ByteArrayOutputStream> cf) {
        // wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?TrustedClientToken=6A5AA1D4EAFF4E9FB37E23D68491D6F4
        // https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/voices/list?trustedclienttoken=6A5AA1D4EAFF4E9FB37E23D68491D6F4
        super(serverUri, httpHeaders);
        this.cf = cf;
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
        log(new String(rawData, StandardCharsets.UTF_8));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log("onClose code:" + code + ", reason:" + reason + ", remote:" + remote);
    }

    @Override
    public void onError(Exception e) {
        cf.complete(new ByteArrayOutputStream());
        log("onClose e:" + e.getMessage());
    }

    static void log(String str) {
        System.out.println(str);
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

    public static ByteArrayOutputStream audioBySsml(String ssml) throws Exception {
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
        }
        client2.send(ssml);
        ByteArrayOutputStream output = cf.get(30, TimeUnit.SECONDS);
        return output;
    }

    public static ByteArrayOutputStream audioByText(String text) throws Exception {
        final String SSML_PATTERN = "X-RequestId:%s\r\n" +
                "Content-Type:application/ssml+xml\r\n" +
                "X-Timestamp:%sZ\r\n" +
                "Path:ssml\r\n" +
                "\r\n" +
                "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xmlns:mstts='https://www.w3.org/2001/mstts' xml:lang='%s'>\r\n" +
                "<voice name='%s'>\r\n" +
                "%s" +
                "<prosody pitch='+0Hz' rate='%s' volume='%s'>" +
                "%s" +
                "</prosody>" +
                "%s" +
                "</voice>" +
                "</speak>";
        String ssml = String.format(
                SSML_PATTERN,
                uuid(),
                date(),
                "zh-CN",
                "zh-CN-XiaoxiaoNeural",
                "",
                "+0%",
                "+0%",
                text,
                ""
        );
        return audioBySsml(ssml);
    }

    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream output = audioByText("你好");
        String name = System.currentTimeMillis() + ".mp3";
        Files.write(new File(
                System.getProperty("user.dir") + "/" + name
        ).toPath(), output.toByteArray());
        log("write " + name);
    }

}
