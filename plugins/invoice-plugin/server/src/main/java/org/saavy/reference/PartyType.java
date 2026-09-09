package org.saavy.reference;

import lombok.Getter;

@Getter
public enum PartyType {
    CUSTOMER("Customer"),
    VENDOR("Vendor"),
    BOTH("Both (Customer & Vendor)");

    private final String label;

    PartyType(String label) {
        this.label = label;
    }

    public static final String getEnumOptions = "[\n" +
            "            { \"label\": \"Customer\", \"value\": \"CUSTOMER\" },\n" +
            "            { \"label\": \"Vendor\", \"value\": \"VENDOR\" },\n" +
            "            { \"label\": \"Both (Customer & Vendor)\", \"value\": \"BOTH\" }\n" +
            "        ]";
}
