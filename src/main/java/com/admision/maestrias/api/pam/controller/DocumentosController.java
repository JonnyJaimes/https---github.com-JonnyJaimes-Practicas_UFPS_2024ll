package com.admision.maestrias.api.pam.controller;

import com.admision.maestrias.api.pam.models.responses.AnyResponse;
import com.admision.maestrias.api.pam.models.responses.DocumentoResponse;
import com.admision.maestrias.api.pam.repository.FileStorageRepository;
import com.admision.maestrias.api.pam.service.implementations.AspiranteService;
import com.admision.maestrias.api.pam.shared.dto.AspiranteDTO;
import com.admision.maestrias.api.pam.shared.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.admision.maestrias.api.pam.shared.dto.DocumentoDTO;

@RestController
@RequestMapping(value = "/documentos")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentosController {

    @Autowired
    private FileStorageRepository fileStorageService; // Cambiado de awsService a fileStorageService

    @Autowired
    private AspiranteService aspiranteService;

    @GetMapping("/listFiles/{idAspirante}")
    public ResponseEntity<List<DocumentoResponse>> listarDocumentosAspirante(@PathVariable Integer idAspirante) throws IOException {
        List<DocumentoResponse> documentoResponses = new ArrayList<>();
        HttpStatus status = HttpStatus.OK;
        try {
            List<DocumentoDTO> documentoDTOs = fileStorageService.listFiles(idAspirante.toString());

            // Convertir DocumentoDTO a DocumentoResponse
            for (DocumentoDTO documentoDTO : documentoDTOs) {
                DocumentoResponse response = new DocumentoResponse();
                response.setKeyFile(documentoDTO.getKeyFile());
                response.setFormato(documentoDTO.getFormato());
                response.setUrl(documentoDTO.getUrl().toString());

                // Asignar estado y tipo de documento si es necesario o manejado en otro lugar
                // response.setEstado(...);
                // response.setDocumento(...);

                documentoResponses.add(response);
            }

        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<>(documentoResponses, status);
    }


    @GetMapping("/downloadFile")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam(value = "tipoDocumento") String tipoDocumento) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getPrincipal().toString();

        byte[] data = fileStorageService.downloadFile(tipoDocumento);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + tipoDocumento + "\"")
                .body(resource);
    }

    @DeleteMapping("/deleteObject")
    public ResponseEntity<AnyResponse> deleteFile(@RequestParam(value = "tipoDocumento") String tipoDocumento) {
        if (fileStorageService.deleteFile(tipoDocumento)) {
            return new ResponseEntity<>(new AnyResponse("File deleted"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new AnyResponse("Ha ocurrido un error al eliminar el documento"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured("ROLE_USUARIO")
    @PostMapping("/uploadFile/{tipoDocumento}")
    public ResponseEntity<AnyResponse> uploadFile(@PathVariable(value = "tipoDocumento") int tipoDocumento,
                                                  @RequestParam(value = "file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int userId = Integer.parseInt(authentication.getDetails().toString());

        try {
            if (fileStorageService.uploadFile(userId + "/" + tipoDocumento, (File) file)) {
                return new ResponseEntity<>(new AnyResponse("Documento subido con éxito"), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new AnyResponse("Ha ocurrido un error al subir el documento"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(new AnyResponse("Error interno: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/downloadFolder")
    public ResponseEntity<Resource> downloadFolder(@RequestParam Integer id) throws IOException {
        Logger logger = LoggerFactory.getLogger(DocumentosController.class);
        logger.info("Descargando carpeta");

        // List<DocumentoDTO> en lugar de List<DocumentoResponse>
        List<DocumentoDTO> fileNames;
        try {
            // fileStorageService.listFiles() devuelve List<DocumentoDTO>
            fileNames = fileStorageService.listFiles(id.toString());
        } catch (Exception e) {
            logger.error("Error al obtener la lista de archivos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        AspiranteDTO aspirante = aspiranteService.getAspiranteByAspiranteId(id);
        UserDTO user = aspiranteService.getUserByAspirante(id);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (DocumentoDTO fileName : fileNames) {
                // Convertir DocumentoDTO a DocumentoResponse si es necesario para la lógica de negocio
                if (fileName.getUrl() != null) {
                    byte[] fileData = fileStorageService.downloadFile(fileName.getKeyFile());
                    ZipEntry zipEntry = new ZipEntry(fileName.getTipoDocumentoDTO().getNombre() + fileName.getFormato());
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(fileData);
                    zipOutputStream.closeEntry();
                }
            }
        } catch (IOException e) {
            logger.error("Error al crear el archivo ZIP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        ByteArrayResource resource = new ByteArrayResource(byteArrayOutputStream.toByteArray());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", aspirante + ".zip");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity
                .ok().headers(headers)
                .contentLength(resource.contentLength())
                .body(resource);
    }

}
