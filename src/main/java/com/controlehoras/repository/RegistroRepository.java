package com.controlehoras.repository;

import com.controlehoras.model.Registro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroRepository extends JpaRepository<Registro, Long> {

    @Query("SELECT r FROM Registro r WHERE " +
           "(:usuarioId IS NULL OR r.usuario.id = :usuarioId) AND " +
           "(:dataExacta IS NULL OR :dataExacta = '' OR r.dataConcreta = :dataExacta) AND " +
           "(:diaSemana IS NULL OR :diaSemana = '' OR LOWER(r.diaSemana) LIKE LOWER(CONCAT('%', :diaSemana, '%'))) AND " +
           "(:inicio IS NULL OR :inicio = '' OR r.dataConcreta >= :inicio) AND " +
           "(:fim IS NULL OR :fim = '' OR r.dataConcreta <= :fim) " +
           "ORDER BY r.id DESC")
    List<Registro> filtrarRegistros(
            @Param("usuarioId") Long usuarioId,
            @Param("dataExacta") String dataExacta,
            @Param("diaSemana") String diaSemana,
            @Param("inicio") String inicio,
            @Param("fim") String fim
    );
}