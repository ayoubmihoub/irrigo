package com.irrigo.botservice.controllers;

import com.irrigo.botservice.services.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/bot")
public class BotController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String message) {
        return ResponseEntity.ok(geminiService.chatWithBot(message));
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyze(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(geminiService.analyzePlantDisease(file.getBytes()));
    }
}