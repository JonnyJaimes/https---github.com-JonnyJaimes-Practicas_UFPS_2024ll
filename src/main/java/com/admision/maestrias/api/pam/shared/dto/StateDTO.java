package com.admision.maestrias.api.pam.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateDTO {
    /**
     * Identificador único del estado
     */
    private Integer id;

    /**
     * Descripción del estado del aspirante
     */
    private AspiranteState description; // Usamos el enum en lugar de String
}
