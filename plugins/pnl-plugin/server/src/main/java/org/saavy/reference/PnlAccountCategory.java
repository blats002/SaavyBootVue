package org.saavy.reference;

public enum PnlAccountCategory {
    REVENUE("Revenue"),
    COGS("Cost of Goods Sold"),
    OPEX("Operating Expense"),
    OTHER_INCOME("Other Income"),
    OTHER_EXPENSE("Other Expense"),
    TAX("Tax");

    private final String label;

    PnlAccountCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

//    public static final String getEnumOptions = "[\n" +
//            "  { \"label\": \"Revenue\", \"value\": \"REVENUE\" },\n" +
//            "  { \"label\": \"Cost of Goods Sold (COGS)\", \"value\": \"COGS\" },\n" +
//            "  { \"label\": \"Operating Expense (OPEX)\", \"value\": \"OPEX\" },\n" +
//            "  { \"label\": \"Other Income\", \"value\": \"OTHER_INCOME\" },\n" +
//            "  { \"label\": \"Other Expense\", \"value\": \"OTHER_EXPENSE\" },\n" +
//            "  { \"label\": \"Tax\", \"value\": \"TAX\" }\n" +
//            "]";
}
