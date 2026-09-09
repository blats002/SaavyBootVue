package org.saavy.reference;

import lombok.Getter;

@Getter
public enum VerificationMethod {
    QR_CODE("QR Code Scanner"),
    PIN("Manual PIN"),
    MANUAL_ADMIN("Admin Adjustment");

    private final String label;

    VerificationMethod(String label) {
        this.label = label;
    }
}
