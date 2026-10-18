package com.controlehoras.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros")
public class Registro {
	@ManyToOne
    @JoinColumn(name = "usuario_id", nullable = true)
	private Usuario usuario;

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String diaSemana;
    private String dataConcreta; // Formato YYYY-MM-DD
    private String modalidade;   // POR_HORA, PRODUCAO, BONUS

    private String horaEntrada;
    private String horaSaida;
    private Integer intervaloMinutos;
    private Integer totalMinutos;
    private String tempoFormatado;
    
    private Integer quantidadePecas;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal valorUnitarioPeca;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorBonus;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorHora;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    private LocalDateTime criadoEm = LocalDateTime.now();

    public Registro() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public String getDataConcreta() { return dataConcreta; }
    public void setDataConcreta(String dataConcreta) { this.dataConcreta = dataConcreta; }

    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }

    public String getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(String horaEntrada) { this.horaEntrada = horaEntrada; }

    public String getHoraSaida() { return horaSaida; }
    public void setHoraSaida(String horaSaida) { this.horaSaida = horaSaida; }

    public Integer getIntervaloMinutos() { return intervaloMinutos; }
    public void setIntervaloMinutos(Integer intervaloMinutos) { this.intervaloMinutos = intervaloMinutos; }

    public Integer getTotalMinutos() { return totalMinutos; }
    public void setTotalMinutos(Integer totalMinutos) { this.totalMinutos = totalMinutos; }

    public String getTempoFormatado() { return tempoFormatado; }
    public void setTempoFormatado(String tempoFormatado) { this.tempoFormatado = tempoFormatado; }

    public Integer getQuantidadePecas() { return quantidadePecas; }
    public void setQuantidadePecas(Integer quantidadePecas) { this.quantidadePecas = quantidadePecas; }

    public BigDecimal getValorUnitarioPeca() { return valorUnitarioPeca; }
    public void setValorUnitarioPeca(BigDecimal valorUnitarioPeca) { this.valorUnitarioPeca = valorUnitarioPeca; }

    public BigDecimal getValorBonus() { return valorBonus; }
    public void setValorBonus(BigDecimal valorBonus) { this.valorBonus = valorBonus; }

    public BigDecimal getValorHora() { return valorHora; }
    public void setValorHora(BigDecimal valorHora) { this.valorHora = valorHora; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}