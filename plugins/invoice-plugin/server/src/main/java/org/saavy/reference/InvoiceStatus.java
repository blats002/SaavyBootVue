package org.saavy.reference;

import lombok.Getter;

@Getter
public enum InvoiceStatus {
    RECEIVED("Received"),
    AWAITING_PAYMENT("Awaiting Payment"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Paid"),
    CANCELLED("Cancelled"),
    OVERPAID("Overpaid"),;

    InvoiceStatus(String label){
        this.label = label;
    }

    String label;


    public final static String getEnumOptions = "[\n" +
            "            { \"label\": \"Received\", \"value\": \"RECEIVED\" },\n" +
            "            { \"label\": \"Awaiting Payment\", \"value\": \"AWAITING_PAYMENT\" },\n" +
            "            { \"label\": \"Partially Paid\", \"value\": \"PARTIALLY_PAID\" },\n" +
            "            { \"label\": \"Paid\", \"value\": \"PAID\" },\n" +
            "            { \"label\": \"Cancelled\", \"value\": \"CANCELLED\" },\n" +
            "            { \"label\": \"Overpaid\", \"value\": \"OVERPAID\" }\n" +
            "        ]";
}
