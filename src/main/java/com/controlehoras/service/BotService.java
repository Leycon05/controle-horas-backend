package com.controlehoras.service;

import com.controlehoras.entity.RegistroPonto;
import com.controlehoras.repository.RegistroPontoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class BotService {

    @Autowired
    private RegistroPontoRepository repository;

    @Autowired
    private EvolutionApiService evolutionApiService;

    @SuppressWarnings("unchecked")
    public void processarWebhook(Map<String, Object> payload) {
        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null) return;

            Map<String, Object> key = (Map<String, Object>) data.get("key");
            if (key == null) return;

            Boolean fromMe = (Boolean) key.get("fromMe");
            // Evita loop infinito processando mensagens enviadas pelo próprio bot
            if (Boolean.TRUE.equals(fromMe)) return;

            String remoteJid = (String) key.get("remoteJid"); // Ex: 5511960163706@s.whatsapp.net
            String numero = remoteJid.replace("@s.whatsapp.net", "").replace("@g.us", "");

            Map<String, Object> message = (Map<String, Object>) data.get("message");
            String textoMensagem = "";
            if (message != null && message.containsKey("conversation")) {
                textoMensagem = (String) message.get("conversation");
            }

            // 1. Salva o registro no banco MySQL (Aiven)
            LocalDateTime agora = LocalDateTime.now();
            RegistroPonto registro = new RegistroPonto(null, numero, agora, textoMensagem);
            repository.save(registro);

            // 2. Formata a resposta de confirmação
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String mensagemResposta = String.format("✅ Ponto registrado com sucesso!\n📅 Horário: %s", agora.format(formatter));

            // 3. Responde ao utilizador no WhatsApp
            evolutionApiService.enviarMensagem(numero, mensagemResposta);

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem no bot: " + e.getMessage());
        }
    }
}