// Path: src/main/java/com/bufalari/cashflow/converter/ManualCashEntryConverter.java
package com.bufalari.cashflow.converter;

import com.bufalari.cashflow.dto.ManualCashEntryDTO;
import com.bufalari.cashflow.entity.ManualCashEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ManualCashEntryConverter {

    public ManualCashEntryDTO entityToDTO(ManualCashEntry entity) {
        if (entity == null) return null;
        return ManualCashEntryDTO.builder()
            .id(entity.getId())
            .entryDate(entity.getEntryDate())
            .amount(entity.getAmount())
            .type(entity.getType())
            .description(entity.getDescription())
            .projectId(entity.getProjectId())
            .costCenterId(entity.getCostCenterId())
            .documentReferences(entity.getDocumentReferences() != null ? new ArrayList<>(entity.getDocumentReferences()) : new ArrayList<>())
            .build();
    }

    public ManualCashEntry dtoToEntity(ManualCashEntryDTO dto) {
         if (dto == null) return null;
         return ManualCashEntry.builder()
            .id(dto.getId()) // Keep ID for updates
            .entryDate(dto.getEntryDate())
            .amount(dto.getAmount())
            .type(dto.getType())
            .description(dto.getDescription())
            .projectId(dto.getProjectId())
            .costCenterId(dto.getCostCenterId())
            .documentReferences(dto.getDocumentReferences() != null ? new ArrayList<>(dto.getDocumentReferences()) : new ArrayList<>())
            .build();
    }
}