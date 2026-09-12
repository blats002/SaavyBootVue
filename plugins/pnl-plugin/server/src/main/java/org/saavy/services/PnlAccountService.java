package org.saavy.services;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.saavy.dto.PnlIngestionResultDTO;
import org.saavy.entity.PnlAccount;
import org.saavy.entity.PnlAccountDTO;
import org.saavy.entity.PnlAccountRepository;
import org.saavy.reference.BaseJpaRepository;
import org.saavy.reference.PnlAccountCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PnlAccountService extends JPAService<PnlAccount, PnlAccountDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PnlAccountRepository pnlAccountRepository;

    @Override
    protected BaseJpaRepository<PnlAccount, Long> getJpaRepository() {
        return pnlAccountRepository;
    }

    public Optional<PnlAccount> findByCode(String code) {
        return pnlAccountRepository.findByCode(code);
    }

    public String generateAccountCsvTemplate() {
        return "code,name,category,subcategory,sort_order,description\n" +
                "4000,Subscription Revenue (MRR/ARR),REVENUE,Recurring,10,Software SaaS subscriptions\n" +
                "4100,Professional Services & Consulting,REVENUE,Services,20,Implementation and consulting revenue\n" +
                "5000,Cloud Hosting & Infrastructure,COGS,Infrastructure,30,AWS / GCP / Cloud computing costs\n" +
                "5100,Payment Gateway Fees,COGS,Merchant Fees,40,Stripe, PayPal, merchant interchange fees\n" +
                "6000,Engineering & Product Salaries,OPEX,R&D,50,Software engineering and QA compensation\n" +
                "6100,Sales & Digital Marketing,OPEX,Sales & Marketing,60,Google/Meta Ads, campaign expenses\n" +
                "6200,Office Rent & Facilities,OPEX,G&A,70,Office leases, utilities, coworking spaces\n" +
                "6300,Legal & Compliance,OPEX,G&A,80,Audit fees, corporate legal, tax compliance\n" +
                "7000,Corporate Income Tax,TAX,Taxes,90,Estimated income tax provisions\n";
    }

    @Transactional
    public PnlIngestionResultDTO importAccountsCsv(InputStream inputStream) {
        PnlIngestionResultDTO result = PnlIngestionResultDTO.builder()
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();

        List<PnlAccount> accountsToSave = new ArrayList<>();
        Map<String, PnlAccount> existingAccounts = new HashMap<>();
        pnlAccountRepository.findAll().forEach(a -> existingAccounts.put(a.getCode().toUpperCase().trim(), a));

        int processed = 0;
        int inserted = 0;

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
                    if (!headerMap.containsKey("code") || !headerMap.containsKey("name")) {
                        result.getErrors().add("CSV header must contain 'code' and 'name' columns.");
                        result.setSuccess(false);
                        return result;
                    }
                    continue;
                }

                processed++;
                try {
                    String code = getVal(tokens, headerMap, "code");
                    String name = getVal(tokens, headerMap, "name");
                    String categoryStr = getVal(tokens, headerMap, "category");
                    String subcategory = getVal(tokens, headerMap, "subcategory");
                    String sortOrderStr = getVal(tokens, headerMap, "sort_order");
                    String description = getVal(tokens, headerMap, "description");

                    if (StringUtils.isBlank(code)) {
                        result.getErrors().add("Row " + lineNumber + ": 'code' is required.");
                        continue;
                    }
                    if (StringUtils.isBlank(name)) {
                        result.getErrors().add("Row " + lineNumber + ": 'name' is required.");
                        continue;
                    }

                    PnlAccountCategory category = PnlAccountCategory.OPEX;
                    if (StringUtils.isNotBlank(categoryStr)) {
                        try {
                            category = PnlAccountCategory.valueOf(categoryStr.trim().toUpperCase().replace(" ", "_"));
                        } catch (IllegalArgumentException e) {
                            result.getWarnings().add("Row " + lineNumber + ": Unknown category '" + categoryStr + "', defaulting to OPEX.");
                        }
                    }

                    Integer sortOrder = 0;
                    if (StringUtils.isNotBlank(sortOrderStr)) {
                        try {
                            sortOrder = Integer.parseInt(sortOrderStr.trim());
                        } catch (NumberFormatException ignored) {}
                    }

                    String codeKey = code.toUpperCase().trim();
                    PnlAccount account = existingAccounts.get(codeKey);
                    if (account == null) {
                        account = new PnlAccount();
                        account.setCode(code.trim());
                        account.setActive(true);
                        existingAccounts.put(codeKey, account);
                        inserted++;
                    }

                    account.setName(name.trim());
                    account.setCategory(category);
                    account.setSubcategory(subcategory);
                    account.setSortOrder(sortOrder);
                    if (StringUtils.isNotBlank(description)) {
                        account.setDescription(description.trim());
                    }

                    accountsToSave.add(account);
                } catch (Exception e) {
                    result.getErrors().add("Row " + lineNumber + ": " + e.getMessage());
                }
            }

            if (!accountsToSave.isEmpty()) {
                pnlAccountRepository.saveAll(accountsToSave);
            }

            result.setTotalProcessed(processed);
            result.setInsertedCount(inserted);
            result.setAccountsCreatedCount(inserted);
            result.setSuccess(result.getErrors().isEmpty());
        } catch (Exception e) {
            result.getErrors().add("Failed to read account CSV: " + e.getMessage());
            result.setSuccess(false);
        }

        return result;
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

    @Override
    public PnlAccountDTO toDTO(PnlAccount entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, PnlAccountDTO.class);
    }

    @Override
    public PnlAccount toEntity(PnlAccountDTO dto, Long id) {
        if (dto == null) return null;
        PnlAccount entity = modelMapper.map(dto, PnlAccount.class);
        entity.setId(id != null ? id : dto.getId());
        return entity;
    }
}
