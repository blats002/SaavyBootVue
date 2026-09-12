package org.saavy.reference;

public enum BusinessType {
    SOLE_PROPRIETOR("Sole Proprietorship / Self-Employed / Freelancer"),
    MIXED_INCOME("Mixed Income Earner (Compensation + Trade/Business)"),
    DOMESTIC_CORPORATION("Domestic Corporation / Partnership (CREATE Act)");

    private final String label;

    BusinessType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
