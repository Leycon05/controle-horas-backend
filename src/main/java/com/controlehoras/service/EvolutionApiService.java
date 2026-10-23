package com.controlehoras.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EvolutionApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Configure no application.properties ou variáveis de ambiente do Render
    @Value("${evolution.api.url:https://evolution-api-controle-horas.onrender.com}")
    private String apiUrl;

    @Value("${evolution.api.key:messi05}")
    private String apiKey;

    @Value("${evolution.api.instance:controle-horas}")
    private String instance;

    public void enviarMensagem(String numero, String texto) {
        String url = String.format("%s/message/sendText/%s", apiUrl, instance);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("number", numero);
        body.put("text", texto);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem via Evolution API: " + e.getMessage());
        }
    }
}