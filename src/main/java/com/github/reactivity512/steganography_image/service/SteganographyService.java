package com.github.reactivity512.steganography_image.service;

import com.galliumdata.adumbra.Decoder;
import com.galliumdata.adumbra.Encoder;
import com.github.reactivity512.steganography_image.config.SteganographyProperties;
import com.github.reactivity512.steganography_image.dto.request.DecodeRequest;
import com.github.reactivity512.steganography_image.dto.request.EncodeRequest;
import com.github.reactivity512.steganography_image.exception.SteganographyException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class SteganographyService {

    private final Encoder encoder;

    private static final int MAX_MESSAGE_BYTES = 32 * 1024; // лимит Adumbra — 32K
    private static final String OUTPUT_FORMAT = "png";

    public SteganographyService(SteganographyProperties props) {
        this.encoder = new Encoder(props.secureLevel());
    }

    public byte[] encode(EncodeRequest req) {
        byte[] message = req.text().getBytes(StandardCharsets.UTF_8);
        byte[] secretKey = req.key().getBytes(StandardCharsets.UTF_8);

        if (message.length > MAX_MESSAGE_BYTES) {
            throw new IllegalArgumentException(
                    "Сообщение слишком велико (макс. " + MAX_MESSAGE_BYTES + " байт)"
            );
        }

        try (InputStream in = req.image().getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            this.encoder.encode(in, out, OUTPUT_FORMAT, message, secretKey);

            return out.toByteArray();

        } catch (Exception e) {
            throw new SteganographyException("Не удалось закодировать сообщение в изображение", e);
        }
    }

    public String decode(DecodeRequest req) {
        byte[] secretKey = req.key().getBytes(StandardCharsets.UTF_8);

        try (InputStream in = req.image().getInputStream()) {
            Decoder decoder = new Decoder();

            byte[] decodedBytes = decoder.decode(in, secretKey);

            if (decodedBytes == null || decodedBytes.length == 0) {
                throw new IllegalStateException("В изображении не найдено скрытое сообщение");
            }

            return new String(decodedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new SteganographyException(
                    "Не удалось извлечь сообщение. Проверьте ключ и целостность PNG-файла.", e
            );
        }
    }
}
