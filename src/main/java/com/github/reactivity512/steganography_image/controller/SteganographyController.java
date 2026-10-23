package com.github.reactivity512.steganography_image.controller;

import com.github.reactivity512.steganography_image.dto.request.DecodeRequest;
import com.github.reactivity512.steganography_image.dto.request.EncodeRequest;
import com.github.reactivity512.steganography_image.service.SteganographyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SteganographyController {

    private final SteganographyService service;

    public SteganographyController(SteganographyService service) {
        this.service = service;
    }

    @RequestMapping("/encode")
    public ResponseEntity<byte[]> encode(@Valid @ModelAttribute EncodeRequest req) {
        byte[] png = service.encode(req);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"encoded.png\"")
                .body(png);
    }

    @PostMapping("/decode")
    public ResponseEntity<String> decode(@Valid @ModelAttribute DecodeRequest req) {
        return ResponseEntity.ok(service.decode(req));
    }
}
