package com.controlehoras.service;

import com.controlehoras.model.Registro;
import com.controlehoras.repository.RegistroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegistroService {

    @Autowired
    private RegistroRepository repository;

    private static final Map<String, String> DIA_ALIASES = new HashMap<>();
    static {
        DIA_ALIASES.put("domingo", "Domingo");
        DIA_ALIASES.put("segunda", "Segunda-feira");
        DIA_ALIASES.put("segunda-feira", "Segunda-feira");
        DIA_ALIASES.put("seg", "Segunda-feira");
        DIA_ALIASES.put("terça", "Terça-feira");
        DIA_ALIASES.put("terca", "Terça-feira");
        DIA_ALIASES.put("terça-feira", "Terça-feira");
        DIA_ALIASES.put("terca-feira", "Terça-feira");
        DIA_ALIASES.put("ter", "Terça-feira");
        DIA_ALIASES.put("quarta", "Quarta-feira");
        DIA_ALIASES.put("quarta-feira", "Quarta-feira");
        DIA_ALIASES.put("qua", "Quarta-feira");
        DIA_ALIASES.put("quinta", "Quinta-feira");
        DIA_ALIASES.put("quinta-feira", "Quinta-feira");
        DIA_ALIASES.put("qui", "Quinta-feira");
        DIA_ALIASES.put("sexta", "Sexta-feira");
        DIA_ALIASES.put("sexta-feira", "Sexta-feira");
        DIA_ALIASES.put("sex", "Sexta-feira");
        DIA_ALIASES.put("sábado", "Sábado");
        DIA_ALIASES.put("sabado", "Sábado");
        DIA_ALIASES.put("sab", "Sábado");
    }

    public List<Registro> filtrar(Long usuarioId, String dataExacta, String diaSemana, String inicio, String fim) {
        return repository.filtrarRegistros(usuarioId, dataExacta, diaSemana, inicio, fim);
    }
    public List<Registro> salvarLote(List<Registro> registros) {
        return repository.saveAll(registros);
    }

    public void deletar(Long id) { repository.deleteById(id); }
    public void deletarTodos() { repository.deleteAll(); }

    public List<Registro> interpretarTexto(String texto, BigDecimal valorHora, String dataReferenciaStr) {
        List<Registro> resultado = new ArrayList<>();
        String[] linhas = texto.split("\r?\n");

        Pattern reDataInline = Pattern.compile("(\\d{1,2})[/.-](\\d{1,2})(?:[/.-](\\d{2,4}))?");
        Pattern reHorarios = Pattern.compile("(\\d{1,2}(?::\\d{2})?\\s*h?)\\s*(?:às|as|a|-|–|—)\\s*(\\d{1,2}(?::\\d{2})?\\s*h?)", Pattern.CASE_INSENSITIVE);
        Pattern reProducao = Pattern.compile("(\\d+)\\s*(?:peças|pecas|unidades|un)\\b(?:\\s*(?:a|x|de)?\\s*(?:R\\$\\s*)?(\\d+(?:[.,]\\d+)?))?", Pattern.CASE_INSENSITIVE);
        Pattern reBonus = Pattern.compile("(?:bônus|bonus|extra|adicional)\\s*(?:de|:)?\\s*(?:R\\$\\s*)?(\\d+(?:[.,]\\d+)?)", Pattern.CASE_INSENSITIVE);

        LocalDate dataRefGlobal = (dataReferenciaStr != null && !dataReferenciaStr.trim().isEmpty()) 
                ? LocalDate.parse(dataReferenciaStr.trim()) : LocalDate.now();

        String diaAtual = null;
        String dataConcretaAtual = null;
        Integer entrada = null, saida = null;
        int intervaloMinutos = 0;
        List<String> observacoes = new ArrayList<>();

        for (String raw : linhas) {
            String linha = raw.trim().replaceAll("^\\*+|\\*+$", "").trim();
            if (linha.isEmpty()) continue;

            // 1. Verificar se a linha traz uma data exata (ex: 15/10/2026 ou 15/10)
            Matcher mData = reDataInline.matcher(linha);
            String dataInlineEncontrada = null;
            if (mData.find()) {
                dataInlineEncontrada = extrairEFormatarData(mData.group(1), mData.group(2), mData.group(3), dataRefGlobal.getYear());
            }

            // 2. Verificar o dia da semana na linha
            String diaIdentificado = extrairDiaSemana(linha);

            if (diaIdentificado != null || dataInlineEncontrada != null) {
                if (entrada != null && saida != null) {
                    resultado.add(criarRegistroHora(diaAtual, dataConcretaAtual, dataRefGlobal, entrada, saida, intervaloMinutos, valorHora, observacoes));
                    entrada = null; saida = null; intervaloMinutos = 0; observacoes.clear();
                }

                if (diaIdentificado != null) diaAtual = diaIdentificado;
                if (dataInlineEncontrada != null) dataConcretaAtual = dataInlineEncontrada;
                continue;
            }

            // 3. Bônus
            Matcher mBonus = reBonus.matcher(linha);
            if (mBonus.find()) {
                BigDecimal vBonus = new BigDecimal(mBonus.group(1).replace(",", "."));
                resultado.add(criarRegistroBonus(diaAtual, dataConcretaAtual, dataRefGlobal, vBonus, linha));
                continue;
            }

            // 4. Produção por Peça
            Matcher mProd = reProducao.matcher(linha);
            if (mProd.find()) {
                int qtd = Integer.parseInt(mProd.group(1));
                BigDecimal vUnit = mProd.group(2) != null ? new BigDecimal(mProd.group(2).replace(",", ".")) : new BigDecimal("2.50");
                resultado.add(criarRegistroProducao(diaAtual, dataConcretaAtual, dataRefGlobal, qtd, vUnit, linha));
                continue;
            }

            // 5. Horários de Trabalho
            Matcher mH = reHorarios.matcher(linha);
            if (mH.find()) {
                if (entrada != null && saida != null) {
                    resultado.add(criarRegistroHora(diaAtual, dataConcretaAtual, dataRefGlobal, entrada, saida, intervaloMinutos, valorHora, observacoes));
                    entrada = null; saida = null; intervaloMinutos = 0; observacoes.clear();
                }
                entrada = parseClock(mH.group(1));
                saida = parseClock(mH.group(2));
                continue;
            }

            // 6. Intervalos
            Integer intervalEncontrado = extrairIntervalo(linha);
            if (intervalEncontrado != null) {
                intervaloMinutos += intervalEncontrado;
                continue;
            }

            observacoes.add(linha);
        }

        if (entrada != null && saida != null) {
            resultado.add(criarRegistroHora(diaAtual, dataConcretaAtual, dataRefGlobal, entrada, saida, intervaloMinutos, valorHora, observacoes));
        }

        return resultado;
    }

    private String extrairDiaSemana(String linha) {
        String limpo = linha.toLowerCase();
        for (Map.Entry<String, String> entry : DIA_ALIASES.entrySet()) {
            if (limpo.contains(entry.getKey())) return entry.getValue();
        }
        return null;
    }

    private String extrairEFormatarData(String diaStr, String mesStr, String anoStr, int anoPadrao) {
        try {
            int dia = Integer.parseInt(diaStr);
            int mes = Integer.parseInt(mesStr);
            int ano = (anoStr != null) ? Integer.parseInt(anoStr) : anoPadrao;
            if (ano < 100) ano += 2000;
            return String.format("%04d-%02d-%02d", ano, mes, dia);
        } catch (Exception e) {
            return null;
        }
    }

    private Registro criarRegistroHora(String dia, String dataConc, LocalDate dataRef, int start, int end, int intMin, BigDecimal rate, List<String> obs) {
        int gross = end - start;
        if (gross <= 0) gross += 24 * 60;
        int netMin = Math.max(0, gross - intMin);

        BigDecimal horasDecimais = BigDecimal.valueOf(netMin).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal pay = horasDecimais.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        Registro reg = new Registro();
        reg.setDiaSemana(dia != null ? dia : "Dia não identificado");
        reg.setDataConcreta(dataConc != null ? dataConc : (dia != null ? calcularDataPorDiaSemana(dia, dataRef) : null));
        reg.setModalidade("POR_HORA");
        reg.setHoraEntrada(formatClock(start));
        reg.setHoraSaida(formatClock(end));
        reg.setIntervaloMinutos(intMin);
        reg.setTotalMinutos(netMin);
        reg.setTempoFormatado((netMin / 60) + "h " + String.format("%02d", netMin % 60) + "min");
        reg.setValorHora(rate);
        reg.setValorTotal(pay);
        reg.setObservacao(String.join(" | ", obs));
        return reg;
    }

    private Registro criarRegistroProducao(String dia, String dataConc, LocalDate dataRef, int qtd, BigDecimal vUnit, String obs) {
        BigDecimal total = vUnit.multiply(BigDecimal.valueOf(qtd)).setScale(2, RoundingMode.HALF_UP);
        Registro reg = new Registro();
        reg.setDiaSemana(dia != null ? dia : "Dia não identificado");
        reg.setDataConcreta(dataConc != null ? dataConc : (dia != null ? calcularDataPorDiaSemana(dia, dataRef) : null));
        reg.setModalidade("PRODUCAO");
        reg.setQuantidadePecas(qtd);
        reg.setValorUnitarioPeca(vUnit);
        reg.setTotalMinutos(0);
        reg.setTempoFormatado(qtd + " peças");
        reg.setValorTotal(total);
        reg.setObservacao(obs);
        return reg;
    }

    private Registro criarRegistroBonus(String dia, String dataConc, LocalDate dataRef, BigDecimal vBonus, String obs) {
        Registro reg = new Registro();
        reg.setDiaSemana(dia != null ? dia : "Dia não identificado");
        reg.setDataConcreta(dataConc != null ? dataConc : (dia != null ? calcularDataPorDiaSemana(dia, dataRef) : null));
        reg.setModalidade("BONUS");
        reg.setValorBonus(vBonus);
        reg.setTotalMinutos(0);
        reg.setTempoFormatado("Bônus Extra");
        reg.setValorTotal(vBonus);
        reg.setObservacao(obs);
        return reg;
    }

    private String calcularDataPorDiaSemana(String diaNome, LocalDate dataRef) {
        List<String> diasOrdem = Arrays.asList("domingo", "segunda", "terça", "quarta", "quinta", "sexta", "sábado");
        String limpo = diaNome.toLowerCase().replace("-feira", "").replace("terca", "terça").replace("sabado", "sábado");
        int targetIdx = diasOrdem.indexOf(limpo);
        if (targetIdx < 0) return null;

        int currentIdx = dataRef.getDayOfWeek().getValue() % 7;
        int delta = targetIdx - currentIdx;
        if (delta < 0) delta += 7;

        return dataRef.plusDays(delta).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private Integer parseClock(String text) {
        if (text == null) return null;
        String cleaned = text.toLowerCase().replaceAll("\\s", "").replaceAll("h$", "");
        String[] partes = cleaned.split(":");
        try {
            int h = Integer.parseInt(partes[0]);
            int min = partes.length > 1 ? Integer.parseInt(partes[1]) : 0;
            return (h > 23 || min > 59) ? null : h * 60 + min;
        } catch (Exception e) { return null; }
    }

    private Integer extrairIntervalo(String linha) {
        String n = linha.toLowerCase().trim();
        if (n.contains("meia hora")) return 30;

        Matcher mH = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*h(?:oras?)?\\b").matcher(n);
        Matcher mM = Pattern.compile("(\\d+)\\s*(?:min|minuto|minutos)\\b").matcher(n);

        int mins = 0;
        boolean achou = false;

        if (mH.find()) {
            mins += (int) Math.round(Double.parseDouble(mH.group(1).replace(",", ".")) * 60);
            achou = true;
        }
        if (mM.find()) {
            mins += Integer.parseInt(mM.group(1));
            achou = true;
        }

        if (!achou) return null;
        if (n.matches(".*(almoço|almoco|janta|jantar|intervalo|refeicao|refeição|lanche|descanso|cafe|café).*")) return mins;
        return null;
    }

    private String formatClock(int mins) {
        return String.format("%02d:%02d", (mins / 60) % 24, mins % 60);
    }
}