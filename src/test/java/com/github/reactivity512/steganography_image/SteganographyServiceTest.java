package com.github.reactivity512.steganography_image;

import com.github.reactivity512.steganography_image.config.SteganographyProperties;
import com.github.reactivity512.steganography_image.dto.request.DecodeRequest;
import com.github.reactivity512.steganography_image.dto.request.EncodeRequest;
import com.github.reactivity512.steganography_image.exception.SteganographyException;
import com.github.reactivity512.steganography_image.service.SteganographyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

public class SteganographyServiceTest {
    private static final String KEY  = "test-secret-key-1234567890";
    private static final String TEXT = "Hello, Steganography!";
    private static final int MIN_KEY = 16;
    private static final int MAX_KEY = 256;

    private SteganographyService service;

    @BeforeEach
    void setUp() {
        SteganographyProperties props = new SteganographyProperties(2, MIN_KEY, MAX_KEY);
        service = new SteganographyService(props);
    }

    private MultipartFile createPng(int w, int h) throws IOException {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                img.setRGB(x, y, (x * 31 + y * 17) & 0xFFFFFF);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return new MockMultipartFile("image", "test.png", "image/png", out.toByteArray());
    }

    @Test
    @DisplayName("round-trip: encode → decode возвращает исходный текст")
    void encodeThenDecode_roundTrip() throws IOException {
        MultipartFile original = createPng(400, 400);

        byte[] encoded = service.encode(new EncodeRequest(original, TEXT, KEY));
        MultipartFile encodedFile = new MockMultipartFile(
                "image", "encoded.png", "image/png", encoded);

        String decoded = service.decode(new DecodeRequest(encodedFile, KEY));

        assertThat(decoded).isEqualTo(TEXT);
    }

    @Test
    @DisplayName("encode возвращает валидный PNG")
    void encode_returnsValidPng() throws IOException {
        MultipartFile original = createPng(400, 400);

        byte[] encoded = service.encode(new EncodeRequest(original, TEXT, KEY));

        assertThat(encoded).isNotEmpty();
        assertThat(encoded[0] & 0xFF).isEqualTo(0x89);
        assertThat(new String(encoded, 1, 3)).isEqualTo("PNG");
        assertThat(ImageIO.read(new ByteArrayInputStream(encoded))).isNotNull();
    }

    @Test
    @DisplayName("кириллица проходит round-trip")
    void encodeThenDecode_cyrillic() throws IOException {
        String cyrillic = "Привет, коллега!";
        MultipartFile original = createPng(400, 400);

        byte[] encoded = service.encode(new EncodeRequest(original, cyrillic, KEY));
        String decoded = service.decode(new DecodeRequest(
                new MockMultipartFile("image", "e.png", "image/png", encoded), KEY));

        assertThat(decoded).isEqualTo(cyrillic);
    }

    @Test
    @DisplayName("decode с неверным ключом → SteganographyException")
    void decode_wrongKey_throws() throws IOException {
        MultipartFile original = createPng(400, 400);
        byte[] encoded = service.encode(new EncodeRequest(original, TEXT, KEY));

        MockMultipartFile encodedFile = new MockMultipartFile(
                "image", "encoded.png", "image/png", encoded);

        assertThatThrownBy(() ->
                service.decode(new DecodeRequest(encodedFile, "wrong-key-but-long-enough")))
                .isInstanceOf(SteganographyException.class);
    }

    @Test
    @DisplayName("decode чистой картинки → SteganographyException")
    void decode_noMessage_throws() throws IOException {
        MultipartFile clean = createPng(400, 400);

        assertThatThrownBy(() -> service.decode(new DecodeRequest(clean, KEY)))
                .isInstanceOf(SteganographyException.class);
    }

    @Test
    @DisplayName("decode мусорных байтов → SteganographyException")
    void decode_garbage_throws() {
        MockMultipartFile garbage = new MockMultipartFile(
                "image", "x.png", "image/png", new byte[]{0, 0, 0, 0});

        assertThatThrownBy(() -> service.decode(new DecodeRequest(garbage, KEY)))
                .isInstanceOf(SteganographyException.class);
    }
}
