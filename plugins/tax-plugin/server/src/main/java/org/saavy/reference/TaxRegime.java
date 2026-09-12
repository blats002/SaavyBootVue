package org.saavy.reference;

public enum TaxRegime {
    FLAT_8_PERCENT("8% Flat Gross Income Tax"),
    OSD_40_PERCENT("40% Optional Standard Deduction (OSD)"),
    ITEMIZED_DEDUCTIONS("Graduated / Itemized Deductions (Actual Audited OPEX)");

    private final String label;

    TaxRegime(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
