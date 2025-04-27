package com.bufalari.cashflow.entity;


import com.bufalari.cashflow.auditing.AuditableBaseEntity;
import com.bufalari.cashflow.enums.EntryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a manual cash entry/exit not directly tied to AP/AR invoices.
 * Representa um lançamento manual de entrada/saída de caixa não ligado diretamente a faturas AP/AR.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "manual_cash_entries")
public class ManualCashEntry extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Type of the entry: CREDIT (inflow) or DEBIT (outflow).
     * Tipo do lançamento: CREDIT (entrada) ou DEBIT (saída).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntryType type;

    /**
     * Description of the manual entry (e.g., "Office supplies", "Owner contribution", "Bank fee").
     * Descrição do lançamento manual (ex: "Material de escritório", "Aporte de sócio", "Tarifa bancária").
     */
    @NotBlank
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String description;

    /**
     * Optional: Link to a project ID if the entry relates to a specific project.
     * Opcional: Link para um ID de projeto se o lançamento se refere a um projeto específico.
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * Optional: Link to a cost center ID if the entry relates to overhead.
     * Opcional: Link para um ID de centro de custo se o lançamento se refere a despesas gerais.
     */
    @Column(name = "cost_center_id")
    private Long costCenterId;

    /**
     * References to supporting documents (receipts, bank slips).
     * Referências a documentos de suporte (recibos, comprovantes bancários).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "manual_entry_doc_references", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "document_reference")
    @Builder.Default
    private List<String> documentReferences = new ArrayList<>();
}