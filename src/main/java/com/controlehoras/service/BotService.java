package com.controlehoras.service;

import com.controlehoras.model.Registro;
import com.controlehoras.model.Usuario;
import com.controlehoras.repository.RegistroRepository;
import com.controlehoras.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
public class BotService {

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
            if (Boolean.TRUE.equals(fromMe)) return; // Ignora mensagens enviadas pelo próprio bot

            String remoteJid = (String) key.get("remoteJid");
            if (remoteJid == null) return;
            String numero = remoteJid.replace("@s.whatsapp.net", "").replace("@g.us", "");

            Map<String, Object> message = (Map<String, Object>) data.get("message");
            String textoMensagem = "";
            if (message != null && message.containsKey("conversation")) {
                textoMensagem = (String) message.get("conversation");
            }

            // 1. Busca o usuário vinculado ao telefone do WhatsApp
            Optional<Usuario> usuarioOpt = usuarioRepository.findByTelefone(numero);

            // 2. Cria e preenche o objeto Registro
            LocalDateTime agora = LocalDateTime.now();
            Registro registro = new Registro();
            
            usuarioOpt.ifPresent(registro::setUsuario);
            
            registro.setDataConcreta(LocalDate.now().toString()); // Formato YYYY-MM-DD
            registro.setHoraEntrada(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            registro.setObservacao("Mensagem do WhatsApp: " + textoMensagem);
            registro.setCriadoEm(agora);

            // 3. Persiste no banco MySQL Aiven
            registroRepository.save(registro);

            // 4. Responde ao usuário pelo WhatsApp via Evolution API
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String mensagemResposta = String.format("✅ Registro de ponto salvo com sucesso!\n📅 Horário: %s", agora.format(formatter));

            evolutionApiService.enviarMensagem(numero, mensagemResposta);

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem no BotService: " + e.getMessage());
        }
    }
}