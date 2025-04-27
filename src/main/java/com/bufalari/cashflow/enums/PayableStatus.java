package com.bufalari.cashflow.enums;

/**
 * Enum representing the possible statuses of an account payable.
 * Copied for use within cash-flow-service DTOs.
 * Enum representando os possíveis status de uma conta a pagar.
 * Copiado para uso dentro dos DTOs do cash-flow-service.
 */
public enum PayableStatus {
    PENDING("Pending", "Pendente"),
    PAID("Paid", "Pago"),
    PARTIALLY_PAID("Partially Paid", "Parcialmente Pago"),
    OVERDUE("Overdue", "Atrasado"),
    CANCELED("Canceled", "Cancelado"),
    IN_NEGOTIATION("In Negotiation", "Em Negociação");

    private final String descriptionEn;
    private final String descriptionPt;

    PayableStatus(String en, String pt) { this.descriptionEn = en; this.descriptionPt = pt; }
    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionPt() { return descriptionPt; }
}