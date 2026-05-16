package jp.igapyon.mikuindexgen.encoding;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Encoding {
    private Encoding() {
    }

    public static String normalizeEncodingName(String value) {
        String normalized = value.trim().toLowerCase().replace("-", "_").replace(" ", "_");

        if ("utf8".equals(normalized) || "utf_8".equals(normalized)) {
            return "utf8";
        }

        if ("shiftjis".equals(normalized)
                || "shift_jis".equals(normalized)
                || "sjis".equals(normalized)
                || "ms_kanji".equals(normalized)
                || "cp932".equals(normalized)
                || "windows_31j".equals(normalized)) {
            return "shift_jis";
        }

        throw new IllegalArgumentException("Unsupported encoding: " + value);
    }

    public static String parseEncodingOption(String value) {
        String encoding = normalizeEncodingName(value);
        charsetForEncoding(encoding);
        return encoding;
    }

    public static String readTextFile(Path filePath, String encoding) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);
        return charsetForEncoding(encoding).decode(ByteBuffer.wrap(bytes)).toString();
    }

    public static void writeTextFile(Path filePath, String content, String encoding) throws IOException {
        CharBuffer chars = CharBuffer.wrap(content);
        ByteBuffer bytes = charsetForEncoding(encoding).encode(chars);
        byte[] output = new byte[bytes.remaining()];
        bytes.get(output);
        Files.write(filePath, output);
    }

    private static Charset charsetForEncoding(String encoding) {
        String normalized = normalizeEncodingName(encoding);
        if ("utf8".equals(normalized)) {
            return StandardCharsets.UTF_8;
        }
        if ("shift_jis".equals(normalized)) {
            return Charset.forName("Windows-31J");
        }
        throw new IllegalArgumentException("Unsupported encoding: " + encoding);
    }
}
