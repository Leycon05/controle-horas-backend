package com.controlehoras.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_ponto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPonto {

    public RegistroPonto(Object object, String numero, LocalDateTime agora, String textoMensagem) {
		// TODO Auto-generated constructor stub
	}
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String telefone;
    private LocalDateTime dataHora;
    private String mensagemOriginal;
}