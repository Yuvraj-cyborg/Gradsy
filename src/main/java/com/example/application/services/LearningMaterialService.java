package com.example.application.services;

import com.example.application.data.LearningMaterial;
import com.example.application.data.LearningMaterialRepository;
import com.example.application.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LearningMaterialService {
    
    @Autowired
    private LearningMaterialRepository learningMaterialRepository;
    
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
    
    public LearningMaterialService() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    
    public List<LearningMaterial> findAllMaterials() {
        return learningMaterialRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<LearningMaterial> findMaterialsByUser(User user) {
        return learningMaterialRepository.findByUploadedBy(user);
    }
    
    public List<LearningMaterial> findMaterialsBySubject(String subject) {
        if (subject == null || subject.trim().isEmpty() || subject.equals("All Subjects")) {
             return learningMaterialRepository.findAllByOrderByCreatedAtDesc(); // Fetch all if no subject or 'All' selected
        } else {
            return learningMaterialRepository.findMaterialsBySubject(subject);
        }
    }
    
    public Optional<LearningMaterial> findById(Long id) {
        return learningMaterialRepository.findById(id);
    }
    
    /**
     * Saves or updates a learning material, handling file storage if an input stream is provided.
     * 
     * @param material The LearningMaterial entity to save/update.
     * @param inputStream The InputStream of the file to upload (can be null if no file).
     * @param fileName The original name of the file (used to create a unique stored name).
     * @param contentType The MIME type of the file.
     * @param contentLength The length of the file content.
     * @param uploader The user uploading the material.
     * @return The saved LearningMaterial entity.
     */
    public LearningMaterial saveLearningMaterial(LearningMaterial material, InputStream inputStream, String fileName, String contentType, long contentLength, User uploader) {
        if (inputStream != null && fileName != null && !fileName.isEmpty()) {
            // Generate a unique file name to prevent collisions
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path targetLocation = fileStorageLocation.resolve(uniqueFileName);

            try {
                // If updating, delete the old file first
                if (material.getId() != null && material.getFilePath() != null) {
                    try {
                         Files.deleteIfExists(fileStorageLocation.resolve(material.getFilePath()));
                    } catch (IOException ex) {
                        System.err.println("Could not delete old file: " + material.getFilePath() + " - " + ex.getMessage());
                        // Log or handle error, but proceed with saving new file
                    }
                }

                // Copy the new file
                Files.copy(inputStream, targetLocation);
                material.setFilePath(uniqueFileName);
            } catch (IOException ex) {
                // Clean up partially created file if copy fails?
                throw new RuntimeException("Could not store file " + uniqueFileName, ex);
            } finally {
                 try { inputStream.close(); } catch (IOException e) { /* ignore close exception */ }
            }
        } else if (material.getId() == null) {
             // If creating new material *without* a file, ensure filePath is null
             material.setFilePath(null);
        }
        // Note: If updating existing material *without* providing a new file stream,
        // we intentionally do NOT nullify the existing filePath.

        material.setUploadedBy(uploader);
        material.setUpdatedAt(LocalDateTime.now());
        return learningMaterialRepository.save(material);
    }
    
    public void deleteMaterial(Long id) {
        LearningMaterial material = findById(id).orElse(null);
        if (material != null && material.getFilePath() != null) {
            try {
                Files.deleteIfExists(fileStorageLocation.resolve(material.getFilePath()));
            } catch (IOException ex) {
                // Log error but continue with DB deletion
                System.err.println("Error deleting file: " + ex.getMessage());
            }
            learningMaterialRepository.deleteById(id);
        }
    }

    public LearningMaterial save(LearningMaterial material) {
        return learningMaterialRepository.save(material);
    }

    public void delete(LearningMaterial material) {
        learningMaterialRepository.delete(material);
    }
} 