package com.bufalari.cashflow.repository;

import com.bufalari.cashflow.entity.ManualCashEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Spring Data JPA repository for ManualCashEntry entities (with UUID ID).
 * Repositório Spring Data JPA para entidades ManualCashEntry (com ID UUID).
 */
@Repository
public interface ManualCashEntryRepository extends JpaRepository<ManualCashEntry, UUID> { // <<<--- Changed to UUID

    /**
     * Finds all manual entries within a specific date range, ordered by date ascending.
     * Encontra todos os lançamentos manuais dentro de um intervalo de datas específico, ordenados por data ascendente.
     */
    List<ManualCashEntry> findByEntryDateBetweenOrderByEntryDateAsc(LocalDate startDate, LocalDate endDate);

    /**
     * Calculates the sum of amounts for manual entries within a date range for CREDIT type. Returns 0 if no entries found.
     * Calcula a soma dos valores para lançamentos manuais dentro de um intervalo de datas para o tipo CRÉDITO. Retorna 0 se nenhum lançamento for encontrado.
     */
    // Ensure the FQN for the enum is correct
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM ManualCashEntry m WHERE m.entryDate >= :startDate AND m.entryDate <= :endDate AND m.type = com.bufalari.cashflow.enums.EntryType.CREDIT")
    BigDecimal sumCreditAmountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Calculates the sum of amounts for manual entries within a date range for DEBIT type. Returns 0 if no entries found.
     * Calcula a soma dos valores para lançamentos manuais dentro de um intervalo de datas para o tipo DÉBITO. Retorna 0 se nenhum lançamento for encontrado.
     */
    // Ensure the FQN for the enum is correct
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM ManualCashEntry m WHERE m.entryDate >= :startDate AND m.entryDate <= :endDate AND m.type = com.bufalari.cashflow.enums.EntryType.DEBIT")
    BigDecimal sumDebitAmountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Optional: Add methods to find by project or cost center if needed
    // List<ManualCashEntry> findByProjectId(Long projectId);
    // List<ManualCashEntry> findByCostCenterId(Long costCenterId);
}