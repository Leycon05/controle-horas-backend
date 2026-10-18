package com.controlehoras.controller;

import com.controlehoras.model.Registro;
import com.controlehoras.model.Usuario;
import com.controlehoras.repository.UsuarioRepository;
import com.controlehoras.service.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook/whatsapp")
@CrossOrigin(origins = "*")
public class WhatsAppWebhookController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RegistroService registroService;

    @PostMapping
    public ResponseEntity<?> receberMensagem(@RequestBody Map<String, Object> payload) {
        try {
            // Extrair telefone e mensagem do JSON padrão enviado pelo Webhook
            String telefone = (String) payload.get("telefone"); 
            String textoMensagem = (String) payload.get("mensagem");

            if (telefone == null || textoMensagem == null) {
                return ResponseEntity.badRequest().body("Payload inválido.");
            }

            // Localizar utilizador pelo número de telefone cadastrado
            Usuario usuario = usuarioRepository.findByTelefone(telefone).orElse(null);
            
            BigDecimal valorHoraPadrao = new BigDecimal("13.00");

            // Interpretar texto e salvar registros associados ao utilizador
            List<Registro> registros = registroService.interpretarTexto(textoMensagem, valorHoraPadrao, null);
            
            if (usuario != null) {
                registros.forEach(r -> r.setUsuario(usuario));
            }
            
            registroService.salvarLote(registros);

            return ResponseEntity.ok("Mensagem processada e registros salvos com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao processar Webhook: " + e.getMessage());
        }
    }
}