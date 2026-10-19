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
            String telefoneRaw = (String) payload.get("telefone"); 
            String textoMensagem = (String) payload.get("mensagem");

            if (telefoneRaw == null || textoMensagem == null) {
                return ResponseEntity.badRequest().body("Payload inválido. Envie os campos 'telefone' e 'mensagem'.");
            }

            // Remove qualquer caractere não numérico (ex: +55 11 99999-9999 -> 5511999999999)
            String telefone = telefoneRaw.replaceAll("[^0-9]", "");

            // 1. Buscar utilizador pelo número cadastrado
            Usuario usuario = usuarioRepository.findByTelefone(telefone).orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(404).body("Utilizador não cadastrado com o telefone: " + telefone);
            }

            BigDecimal valorHoraPadrao = new BigDecimal("13.00");

            // 2. Interpretar texto da mensagem
            List<Registro> registros = registroService.interpretarTexto(textoMensagem, valorHoraPadrao, null);

            if (registros.isEmpty()) {
                return ResponseEntity.badRequest().body("Nenhum registro de horas válido foi identificado no texto.");
            }

            // 3. Associar utilizador e salvar lote
            registros.forEach(r -> r.setUsuario(usuario));
            List<Registro> salvos = registroService.salvarLote(registros);

            return ResponseEntity.ok(Map.of(
                "status", "sucesso",
                "mensagem", salvos.size() + " registro(s) salvo(s) com sucesso para " + usuario.getNome(),
                "registros", salvos
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao processar Webhook: " + e.getMessage());
        }
    }
}