// Path: src/main/java/com/bufalari/cashflow/enums/EntryType.java
package com.bufalari.cashflow.enums;

/**
 * Enum indicating whether a cash flow entry represents an inflow or outflow.
 * Enum indicando se um lançamento de fluxo de caixa representa entrada ou saída.
 */
public enum EntryType {
    DEBIT("Debit", "Débito"),   // Represents cash outflow / Representa saída de caixa
    CREDIT("Credit", "Crédito"); // Represents cash inflow / Representa entrada de caixa

    private final String descriptionEn;
    private final String descriptionPt;

    EntryType(String en, String pt) { this.descriptionEn = en; this.descriptionPt = pt; }
    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionPt() { return descriptionPt; }
}