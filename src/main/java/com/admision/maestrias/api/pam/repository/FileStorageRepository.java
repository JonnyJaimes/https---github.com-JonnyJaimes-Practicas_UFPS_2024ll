package com.admision.maestrias.api.pam.repository;

import com.admision.maestrias.api.pam.shared.dto.DocumentoDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileStorageRepository {
    List<DocumentoDTO> listFiles(String directoryPath);
    InputStream getFileInputStream(String filePath) throws IOException;
    byte[] downloadFile(String filePath) throws IOException;
    boolean deleteFile(String filePath);
    boolean uploadFile(String filePath, File fileObj) throws IOException;
    boolean createDirectory(String directoryPath);
    String getFileUrl(String filePath);
}
