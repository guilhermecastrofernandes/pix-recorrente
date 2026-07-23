package com.pix.recorrente.repository;

import com.pix.recorrente.domain.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {
    Optional<Agendamento> findByChaveIdempotencia(String chaveIdempotencia);
}
