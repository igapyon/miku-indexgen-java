package jp.igapyon.mikuindexgen.encoding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EncodingTest {
    @TempDir
    Path tempDir;

    @Test
    void parseEncodingOptionNormalizesSupportedEncodingAliases() {
        assertEquals("utf8", Encoding.parseEncodingOption("utf-8"));
        assertEquals("shift_jis", Encoding.parseEncodingOption("ShiftJIS"));
        assertEquals("shift_jis", Encoding.parseEncodingOption("cp932"));
    }

    @Test
    void parseEncodingOptionRejectsUnsupportedEncodings() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Encoding.parseEncodingOption("euc-jp"));
        assertEquals("Unsupported encoding: euc-jp", ex.getMessage());
    }

    @Test
    void writeTextFileWritesTextWithTheSpecifiedEncoding() throws Exception {
        Path filePath = tempDir.resolve("sample.txt");

        Encoding.writeTextFile(filePath, "日本語", "shift_jis");

        assertArrayEquals("日本語".getBytes(Charset.forName("Windows-31J")), Files.readAllBytes(filePath));
    }
}
