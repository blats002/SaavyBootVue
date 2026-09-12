package org.saavy.services;

import org.apache.commons.lang3.StringUtils;
import org.saavy.dto.PnlIngestionBatchDTO;
import org.saavy.dto.PnlIngestionDTO;
import org.saavy.dto.PnlIngestionResultDTO;
import org.saavy.entity.PnlAccount;
import org.saavy.entity.PnlAccountRepository;
import org.saavy.entity.PnlLedgerEntry;
import org.saavy.entity.PnlLedgerEntryRepository;
import org.saavy.reference.PnlAccountCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class PnlCsvIngestionService {

    @Autowired
    private PnlAccountRepository pnlAccountRepository;

    @Autowired
    private PnlLedgerEntryRepository pnlLedgerEntryRepository;

    public static final String CSV_HEADER = "date,account_code,account_name,category,subcategory,amount,currency,entity,reference_id,description";

    public String generateSampleCsvTemplate() {
        return CSV_HEADER + "\n" +
                "2026-03-31,4000,Subscription Revenue (MRR/ARR),REVENUE,Recurring,52000.00,USD,Main Corp,MRR-MAR26,March 2026 Recurring Revenue\n" +
                "2026-03-25,4100,Professional Services & Consulting,REVENUE,Services,12000.00,USD,Main Corp,SRV-MAR26,Strategic Consulting Project\n" +
                "2026-03-31,5000,Cloud Hosting & Infrastructure,COGS,Infrastructure,5600.00,USD,Main Corp,AWS-MAR26,AWS Production Infrastructure\n" +
                "2026-03-31,5100,Payment Gateway Fees,COGS,Merchant Fees,1620.00,USD,Main Corp,STRP-MAR26,Stripe Merchant Fees\n" +
                "2026-03-31,6000,Engineering & Product Salaries,OPEX,R&D,22500.00,USD,Main Corp,PAY-RD-MAR26,Engineering Payroll March\n" +
                "2026-03-31,6100,Sales & Digital Marketing,OPEX,Sales & Marketing,8100.00,USD,Main Corp,MKT-MAR26,Paid Search Campaigns\n" +
                "2026-03-01,6200,Office Rent & Facilities,OPEX,G&A,3500.00,USD,Main Corp,RENT-MAR26,HQ Office Lease\n" +
                "2026-03-15,6300,\"Legal, Accounting & Compliance\",OPEX,G&A,1500.00,USD,Main Corp,LGL-MAR26,Annual Audit Advisory\n" +
                "2026-03-31,7000,Corporate Income Tax,TAX,Taxes,3200.00,USD,Main Corp,TAX-MAR26,Estimated Income Tax Provision\n";
    }

    public List<PnlIngestionDTO> parseCsv(InputStream inputStream, List<String> parseErrors) {
        List<PnlIngestionDTO> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            boolean isFirstLine = true;
            Map<String, Integer> headerMap = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (StringUtils.isBlank(line)) continue;

                List<String> tokens = parseCsvLine(line);
                if (tokens.isEmpty()) continue;

                if (isFirstLine) {
                    isFirstLine = false;
                    for (int i = 0; i < tokens.size(); i++) {
                        headerMap.put(tokens.get(i).trim().toLowerCase(), i);
                    }
                    if (!headerMap.containsKey("account_code") && !headerMap.containsKey("amount")) {
                        parseErrors.add("CSV header missing required columns 'account_code' or 'amount'. Found: " + line);
                        return records;
                    }
                    continue;
                }

                try {
                    PnlIngestionDTO dto = mapTokensToDTO(tokens, headerMap, lineNumber);
                    if (dto != null) {
                        records.add(dto);
                    }
                } catch (Exception e) {
                    parseErrors.add("Row " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            parseErrors.add("Failed to read CSV stream: " + e.getMessage());
        }
        return records;
    }

    @Transactional
    public PnlIngestionResultDTO ingestBatch(PnlIngestionBatchDTO batchDTO) {
        PnlIngestionResultDTO result = PnlIngestionResultDTO.builder()
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();

        if (batchDTO == null || batchDTO.getRecords() == null || batchDTO.getRecords().isEmpty()) {
            result.setSuccess(true);
            result.setTotalProcessed(0);
            return result;
        }

        Map<String, PnlAccount> accountCache = new HashMap<>();
        pnlAccountRepository.findAll().forEach(acc -> accountCache.put(acc.getCode().toUpperCase().trim(), acc));

        int inserted = 0;
        int accountsCreated = 0;
        List<PnlLedgerEntry> entriesToSave = new ArrayList<>();

        for (int i = 0; i < batchDTO.getRecords().size(); i++) {
            PnlIngestionDTO item = batchDTO.getRecords().get(i);
            int rowNum = i + 1;

            if (item.getAmount() == null) {
                result.getErrors().add("Row " + rowNum + ": Amount cannot be null.");
                continue;
            }
            if (item.getDate() == null) {
                result.getErrors().add("Row " + rowNum + ": Date cannot be null.");
                continue;
            }
            if (StringUtils.isBlank(item.getAccountCode())) {
                result.getErrors().add("Row " + rowNum + ": Account code cannot be empty.");
                continue;
            }

            String codeKey = item.getAccountCode().toUpperCase().trim();
            PnlAccount account = accountCache.get(codeKey);

            if (account == null) {
                // Auto-create account if not found
                PnlAccountCategory category = item.getCategory() != null ? item.getCategory() : PnlAccountCategory.OPEX;
                String accountName = StringUtils.isNotBlank(item.getAccountName()) ? item.getAccountName().trim() : ("Account " + item.getAccountCode());
                
                account = new PnlAccount();
                account.setCode(item.getAccountCode().trim());
                account.setName(accountName);
                account.setCategory(category);
                account.setSubcategory(item.getSubcategory());
                account.setSortOrder(99);
                account.setActive(true);
                account = pnlAccountRepository.save(account);
                accountCache.put(codeKey, account);
                accountsCreated++;
                result.getWarnings().add("Auto-created new Account [" + account.getCode() + "] " + account.getName() + " (" + category + ")");
            }

            PnlLedgerEntry entry = new PnlLedgerEntry();
            entry.setAccount(account);
            entry.setEntryDate(item.getDate());
            entry.setAmount(item.getAmount());
            entry.setCurrency(StringUtils.isNotBlank(item.getCurrency()) ? item.getCurrency().trim() : "USD");
            entry.setEntityName(StringUtils.isNotBlank(item.getEntity()) ? item.getEntity().trim() : "Main");
            entry.setReferenceId(item.getReferenceId());
            entry.setDescription(item.getDescription());
            entriesToSave.add(entry);
            inserted++;
        }

        if (!entriesToSave.isEmpty()) {
            pnlLedgerEntryRepository.saveAll(entriesToSave);
        }

        result.setSuccess(result.getErrors().isEmpty());
        result.setTotalProcessed(batchDTO.getRecords().size());
        result.setInsertedCount(inserted);
        result.setAccountsCreatedCount(accountsCreated);
        return result;
    }

    private PnlIngestionDTO mapTokensToDTO(List<String> tokens, Map<String, Integer> headerMap, int rowNum) {
        String dateStr = getVal(tokens, headerMap, "date");
        String accountCode = getVal(tokens, headerMap, "account_code");
        String accountName = getVal(tokens, headerMap, "account_name");
        String categoryStr = getVal(tokens, headerMap, "category");
        String subcategory = getVal(tokens, headerMap, "subcategory");
        String amountStr = getVal(tokens, headerMap, "amount");
        String currency = getVal(tokens, headerMap, "currency");
        String entity = getVal(tokens, headerMap, "entity");
        String referenceId = getVal(tokens, headerMap, "reference_id");
        String description = getVal(tokens, headerMap, "description");

        if (StringUtils.isBlank(accountCode)) {
            throw new IllegalArgumentException("Account code is missing");
        }
        if (StringUtils.isBlank(amountStr)) {
            throw new IllegalArgumentException("Amount is missing");
        }

        LocalDate date;
        try {
            if (dateStr.length() == 7) { // yyyy-MM -> end of month
                YearMonth ym = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM"));
                date = ym.atEndOfMonth();
            } else {
                date = LocalDate.parse(dateStr);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format '" + dateStr + "'. Expected YYYY-MM-DD or YYYY-MM.");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr.replaceAll("[,\\s$]", ""));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid numeric amount '" + amountStr + "'.");
        }

        PnlAccountCategory category = null;
        if (StringUtils.isNotBlank(categoryStr)) {
            try {
                category = PnlAccountCategory.valueOf(categoryStr.trim().toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException ignored) {}
        }

        return PnlIngestionDTO.builder()
                .date(date)
                .accountCode(accountCode)
                .accountName(accountName)
                .category(category)
                .subcategory(subcategory)
                .amount(amount)
                .currency(currency)
                .entity(entity)
                .referenceId(referenceId)
                .description(description)
                .build();
    }

    private String getVal(List<String> tokens, Map<String, Integer> headerMap, String key) {
        Integer index = headerMap.get(key);
        if (index != null && index < tokens.size()) {
            return tokens.get(index).trim();
        }
        return "";
    }

    private List<String> parseCsvLine(String line) {
        List<String> list = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString());
        return list;
    }
}
