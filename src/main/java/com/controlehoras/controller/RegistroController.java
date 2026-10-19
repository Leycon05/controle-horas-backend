package com.controlehoras.controller;

import com.controlehoras.model.Registro;
import com.controlehoras.service.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registros")
@CrossOrigin(origins = "*")
public class RegistroController {

    @Autowired
    private RegistroService service;

    @GetMapping
    public List<Registro> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String dataExacta,
            @RequestParam(required = false) String diaSemana,
            @RequestParam(required = false) String inicio,
            @RequestParam(required = false) String fim) {
        return service.filtrar(usuarioId, dataExacta, diaSemana, inicio, fim);
    }

    @PostMapping("/interpretar")
    public List<Registro> interpretar(@RequestBody Map<String, Object> body) {
        String texto = (String) body.get("texto");
        BigDecimal valorHora = new BigDecimal(body.getOrDefault("valorHora", "13.00").toString());
        String dataRef = (String) body.get("dataReferencia");
        return service.interpretarTexto(texto, valorHora, dataRef);
    }

    @PostMapping("/salvar-lote")
    public List<Registro> salvarLote(@RequestBody List<Registro> registros) {
        return service.salvarLote(registros);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) { service.deletar(id); }

    @DeleteMapping
    public void deletarTodos() { service.deletarTodos(); }
}