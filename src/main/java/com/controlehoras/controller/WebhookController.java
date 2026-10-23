package com.controlehoras.controller;

import com.controlehoras.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    @Autowired
    private BotService botService;

    @PostMapping("/whatsapp")
    public ResponseEntity<Void> receberWebhook(@RequestBody Map<String, Object> payload) {
        botService.processarWebhook(payload);
        return ResponseEntity.ok().build();
    }
}