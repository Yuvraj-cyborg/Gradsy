package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
 
@Repository
public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {
    List<LearningMaterial> findByUploadedBy(User user);
    List<LearningMaterial> findAllByOrderByCreatedAtDesc();

    // Find materials optionally filtered by teacher's subject area
    @Query("SELECT lm FROM LearningMaterial lm JOIN lm.uploadedBy u JOIN TeacherProfile tp ON tp.user = u WHERE (:subject IS NULL OR tp.subjectArea = :subject)")
    List<LearningMaterial> findMaterialsBySubject(@Param("subject") String subject);
} 