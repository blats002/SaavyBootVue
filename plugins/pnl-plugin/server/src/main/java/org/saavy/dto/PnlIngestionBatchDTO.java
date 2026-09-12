package org.saavy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PnlIngestionBatchDTO implements Serializable {
    private String sourceName;
    private List<PnlIngestionDTO> records = new ArrayList<>();
}
