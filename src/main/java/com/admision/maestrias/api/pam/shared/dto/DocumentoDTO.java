package com.admision.maestrias.api.pam.shared.dto;

import lombok.*;

import java.net.URL;

/**
 * DTO de DocumentoEntity
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentoDTO {
    /**
     * Clave del archivo en el sistema de almacenamiento
     */
    private String keyFile;
    /**
     * URL del archivo en el sistema de almacenamiento
     */
    private URL url;
    /**
     * Nombre original del archivo que el usuario proporcionó
     */
    private String name;
    /**
     * Extensión del archivo (por ejemplo, jpg, pdf)
     */
    private String formato;
    /**
     * Tipo de documento (por ejemplo, foto, diploma, etc.)
     */
    private TipoDocumentoDTO tipoDocumentoDTO;
    /**
     * Retroalimentación sobre el documento (si aplica)
     */
    private String retroalimentacion;
}
