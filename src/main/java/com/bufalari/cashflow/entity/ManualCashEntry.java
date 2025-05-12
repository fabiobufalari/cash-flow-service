package com.bufalari.cashflow.entity;

import com.bufalari.cashflow.auditing.AuditableBaseEntity;
import com.bufalari.cashflow.enums.EntryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin; // Para validação no nível da entidade (opcional aqui)
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Import Objects
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Represents a manual cash entry/exit not directly tied to AP/AR invoices. Uses UUID for ID.
 * Representa um lançamento manual de entrada/saída de caixa não ligado diretamente a faturas AP/AR. Usa UUID para ID.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "manual_cash_entries", indexes = { // Adiciona índices relevantes
        @Index(name = "idx_manual_entry_date", columnList = "entryDate"),
        @Index(name = "idx_manual_entry_type", columnList = "type")
})
public class ManualCashEntry extends AuditableBaseEntity {

    private static final Logger log = LoggerFactory.getLogger(ManualCashEntry.class);

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid") // Define tipo no DB
    private UUID id; // <<<--- Changed to UUID

    /**
     * Date the transaction occurred or should be accounted for.
     * Data em que a transação ocorreu ou deve ser contabilizada.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDate entryDate;

    /**
     * The amount of the transaction. Always positive. Type determines inflow/outflow.
     * O valor da transação. Sempre positivo. O tipo determina entrada/saída.
     */
    @NotNull
    // @DecimalMin(value = "0.0", inclusive = false) // Validação pode ser feita em @PrePersist/Update
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Type of the entry: CREDIT (inflow) or DEBIT (outflow).
     * Tipo do lançamento: CREDIT (entrada) ou DEBIT (saída).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10) // Tamanho ajustado para DEBIT/CREDIT
    private EntryType type;

    /**
     * Description of the manual entry (e.g., "Office supplies", "Owner contribution", "Bank fee").
     * Descrição do lançamento manual (ex: "Material de escritório", "Aporte de sócio", "Tarifa bancária").
     */
    @NotBlank // Garante que não seja nulo nem apenas espaços
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String description;

    /**
     * Optional: Link to a project ID if the entry relates to a specific project.
     * Opcional: Link para um ID de projeto se o lançamento se refere a um projeto específico.
     */
    @Column(name = "project_id")
    private Long projectId; // Remains Long

    /**
     * Optional: Link to a cost center ID if the entry relates to overhead.
     * Opcional: Link para um ID de centro de custo se o lançamento se refere a despesas gerais.
     */
    @Column(name = "cost_center_id")
    private Long costCenterId; // Remains Long

    /**
     * References to supporting documents (receipts, bank slips).
     * Referências a documentos de suporte (recibos, comprovantes bancários).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "manual_entry_doc_references", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "document_reference")
    @Builder.Default
    private List<String> documentReferences = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void validateAmount() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Manual cash entry amount must be positive. Found: {}", amount);
            throw new IllegalArgumentException("Manual cash entry amount must be positive.");
        }
    }

    // --- equals() and hashCode() based on ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualCashEntry that = (ManualCashEntry) o;
        return id != null ? id.equals(that.id) : super.equals(o);
    }

    @Override
    public int hashCode() {
        // Se id não for nulo, use seu hash, senão use o hash padrão do objeto
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}