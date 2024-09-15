
package com.admision.maestrias.api.pam.models.requests;

import java.util.Map;
import com.admision.maestrias.api.pam.shared.dto.DocumentoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DocumentoRequest {
    /**
     * Mapa de documentos con clave como tipo de documento y valor como el archivo correspondiente.
     */
    private Map<String, DocumentoDTO> documentos;
}
