package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByStudent(User student);
    List<QuizAttempt> findByQuiz(Quiz quiz);
    List<QuizAttempt> findByQuizAndStudentAndCompletedTrue(Quiz quiz, User student);
    boolean existsByQuizAndStudentAndCompletedTrue(Quiz quiz, User student);

    // New method to fetch attempt with quiz and its questions
    @Query("SELECT DISTINCT qa FROM QuizAttempt qa JOIN FETCH qa.quiz q LEFT JOIN FETCH q.questions WHERE qa.id = :id")
    Optional<QuizAttempt> findByIdAndFetchQuizWithQuestions(@Param("id") Long id);
} 