// Path: src/main/java/com/bufalari/cashflow/repository/ManualCashEntryRepository.java
package com.bufalari.cashflow.repository;

import com.bufalari.cashflow.entity.ManualCashEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ManualCashEntryRepository extends JpaRepository<ManualCashEntry, Long> {

    /**
     * Finds all manual entries within a specific date range.
     * Encontra todos os lançamentos manuais dentro de um intervalo de datas específico.
     */
    List<ManualCashEntry> findByEntryDateBetweenOrderByEntryDateAsc(LocalDate startDate, LocalDate endDate);

    /**
     * Calculates the sum of amounts for manual entries within a date range for a specific type (CREDIT/DEBIT).
     * Calcula a soma dos valores para lançamentos manuais dentro de um intervalo de datas para um tipo específico (CRÉDITO/DÉBITO).
     */
    @Query("SELECT SUM(m.amount) FROM ManualCashEntry m WHERE m.entryDate >= :startDate AND m.entryDate <= :endDate AND m.type = com.bufalari.cashflow.enums.EntryType.CREDIT")
    BigDecimal sumCreditAmountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(m.amount) FROM ManualCashEntry m WHERE m.entryDate >= :startDate AND m.entryDate <= :endDate AND m.type = com.bufalari.cashflow.enums.EntryType.DEBIT")
    BigDecimal sumDebitAmountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}