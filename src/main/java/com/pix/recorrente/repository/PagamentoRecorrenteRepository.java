package com.pix.recorrente.repository;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagamentoRecorrenteRepository extends JpaRepository<PagamentoRecorrente, UUID> {
    List<PagamentoRecorrente> findByAgendamentoId(UUID agendamentoId);
    Optional<PagamentoRecorrente> findByChaveIdempotencia(String chaveIdempotencia);

    @Query("SELECT p FROM PagamentoRecorrente p WHERE p.status = :status AND p.dataPrevista <= :data")
    List<PagamentoRecorrente> findPendentes(@Param("status") EnumStatusPagamento status, @Param("data") LocalDate data);
}
