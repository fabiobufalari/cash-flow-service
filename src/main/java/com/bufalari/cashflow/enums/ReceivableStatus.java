// Path: src/main/java/com/bufalari/cashflow/enums/ReceivableStatus.java
package com.bufalari.cashflow.enums;

/**
 * Enum representing the possible statuses of an account receivable.
 * Copied for use within cash-flow-service DTOs.
 * Enum representando os possíveis status de uma conta a receber.
 * Copiado para uso dentro dos DTOs do cash-flow-service.
 */
public enum ReceivableStatus {
    PENDING("Pending", "Pendente"),
    RECEIVED("Received", "Recebido"),
    PARTIALLY_RECEIVED("Partially Received", "Parcialmente Recebido"),
    OVERDUE("Overdue", "Atrasado"),
    IN_DISPUTE("In Dispute", "Em Disputa"),
    WRITTEN_OFF("Written Off", "Baixado"),
    CANCELED("Canceled", "Cancelado");

    private final String descriptionEn;
    private final String descriptionPt;

    ReceivableStatus(String en, String pt) { this.descriptionEn = en; this.descriptionPt = pt; }
    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionPt() { return descriptionPt; }
}