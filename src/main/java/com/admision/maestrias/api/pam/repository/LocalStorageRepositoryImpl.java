package com.admision.maestrias.api.pam.repository;

import com.admision.maestrias.api.pam.shared.dto.DocumentoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LocalStorageRepositoryImpl implements FileStorageRepository {

    @Value("${local.storage.basepath}")
    private String basePath;

    @Override
    public List<DocumentoDTO> listFiles(String directoryPath) {
        List<DocumentoDTO> items = new ArrayList<>();
        try {
            Files.walk(Paths.get(basePath, directoryPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        DocumentoDTO dto = new DocumentoDTO();
                        dto.setKeyFile(path.getFileName().toString());
                        try {
                            dto.setUrl(path.toUri().toURL());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            // Maneja la excepción apropiadamente
                        }

                        items.add(dto);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public InputStream getFileInputStream(String filePath) throws IOException {
        Path fullPath = Paths.get(basePath, filePath);
        if (Files.exists(fullPath)) {
            return new FileInputStream(fullPath.toFile());
        } else {
            throw new FileNotFoundException("Archivo no encontrado: " + fullPath.toString());
        }
    }

    @Override
    public byte[] downloadFile(String filePath) throws IOException {
        Path fullPath = Paths.get(basePath, filePath);
        if (Files.exists(fullPath)) {
            return Files.readAllBytes(fullPath);
        } else {
            throw new FileNotFoundException("Archivo no encontrado: " + fullPath.toString());
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path fullPath = Paths.get(basePath, filePath);
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean uploadFile(String filePath, File fileObj) throws IOException {
        Path targetPath = Paths.get(basePath, filePath);
        Files.createDirectories(targetPath.getParent());
        Files.copy(fileObj.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        fileObj.delete(); // Elimina el archivo temporal
        return true;
    }

    @Override
    public boolean createDirectory(String directoryPath) {
        try {
            Path dirPath = Paths.get(basePath, directoryPath);
            Files.createDirectories(dirPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        Path fullPath = Paths.get(basePath, filePath);
        if (Files.exists(fullPath)) {
            return fullPath.toUri().toString();
        } else {
            return null;
        }
    }
}
