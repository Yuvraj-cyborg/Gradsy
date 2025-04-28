package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCreatedBy(User user);
    List<Quiz> findByIsActiveTrue();
    List<Quiz> findAllByOrderByCreatedAtDesc();

    // Find active quizzes optionally filtered by teacher's subject area
    @Query("SELECT q FROM Quiz q JOIN q.createdBy u JOIN TeacherProfile tp ON tp.user = u WHERE q.isActive = true AND (:subject IS NULL OR tp.subjectArea = :subject)")
    List<Quiz> findActiveQuizzesBySubject(@Param("subject") String subject);

    // New method to fetch Quiz with Questions
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);
} 