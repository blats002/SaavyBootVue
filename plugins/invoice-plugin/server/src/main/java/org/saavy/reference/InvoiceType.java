package org.saavy.reference;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
public enum InvoiceType {

    RECEIVABLE("Receivable"),
    PAYABLE("Payable");

    private final String label;

    InvoiceType(String label) {
        this.label = label;
    }

    public final static String getEnumOptions = "[\n" +
            "            { \"label\": \"Receivable\", \"value\": \"RECEIVABLE\" },\n" +
            "            { \"label\": \"Payable\", \"value\": \"PAYABLE\" }\n" +
            "        ]";

}

