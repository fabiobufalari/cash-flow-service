package com.bufalari.cashflow.converter;

import com.bufalari.cashflow.dto.ManualCashEntryDTO;
import com.bufalari.cashflow.entity.ManualCashEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
// UUID import não é necessário aqui, pois o tipo é usado implicitamente

/**
 * Converts between ManualCashEntry (with UUID ID) and ManualCashEntryDTO (with UUID ID).
 * Converte entre ManualCashEntry (com ID UUID) e ManualCashEntryDTO (com ID UUID).
 */
@Component
public class ManualCashEntryConverter {

    /**
     * Converts ManualCashEntry entity to ManualCashEntryDTO.
     * Converte entidade ManualCashEntry para ManualCashEntryDTO.
     * @param entity The entity (with UUID ID). / A entidade (com ID UUID).
     * @return The DTO (with UUID ID). / O DTO (com ID UUID).
     */
    public ManualCashEntryDTO entityToDTO(ManualCashEntry entity) {
        if (entity == null) return null;
        return ManualCashEntryDTO.builder()
                .id(entity.getId()) // <<<--- UUID
                .entryDate(entity.getEntryDate())
                .amount(entity.getAmount())
                .type(entity.getType())
                .description(entity.getDescription())
                .projectId(entity.getProjectId()) // Remains Long
                .costCenterId(entity.getCostCenterId()) // Remains Long
                .documentReferences(entity.getDocumentReferences() != null ? new ArrayList<>(entity.getDocumentReferences()) : new ArrayList<>())
                .build();
    }

    /**
     * Converts ManualCashEntryDTO to ManualCashEntry entity.
     * Converte ManualCashEntryDTO para entidade ManualCashEntry.
     * @param dto The DTO (with UUID ID). / O DTO (com ID UUID).
     * @return The entity (with UUID ID). / A entidade (com ID UUID).
     */
    public ManualCashEntry dtoToEntity(ManualCashEntryDTO dto) {
        if (dto == null) return null;
        return ManualCashEntry.builder()
                .id(dto.getId()) // <<<--- UUID (Keep ID for updates)
                .entryDate(dto.getEntryDate())
                .amount(dto.getAmount())
                .type(dto.getType())
                .description(dto.getDescription())
                .projectId(dto.getProjectId()) // Remains Long
                .costCenterId(dto.getCostCenterId()) // Remains Long
                .documentReferences(dto.getDocumentReferences() != null ? new ArrayList<>(dto.getDocumentReferences()) : new ArrayList<>())
                .build();
    }
}