package com.controlehoras.repository;

import com.controlehoras.entity.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {
}