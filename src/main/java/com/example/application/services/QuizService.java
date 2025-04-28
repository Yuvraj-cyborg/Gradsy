package com.example.application.services;

import com.example.application.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;
    
    public List<Quiz> findAllQuizzes() {
        return quizRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Quiz> findActiveQuizzes(String subject) {
        if (subject == null || subject.trim().isEmpty() || subject.equals("All Subjects")) {
            return quizRepository.findByIsActiveTrue(); // Fetch all active if no subject or 'All'
        } else {
            return quizRepository.findActiveQuizzesBySubject(subject);
        }
    }
    
    public List<Quiz> findActiveQuizzes() {
        return quizRepository.findByIsActiveTrue();
    }
    
    public List<Quiz> findQuizzesByCreator(User user) {
        return quizRepository.findByCreatedBy(user);
    }
    
    public Quiz findById(Long id) {
        return quizRepository.findById(id).orElse(null);
    }
    
    public Optional<Quiz> findByIdWithQuestions(Long id) {
        return quizRepository.findByIdWithQuestions(id);
    }
    
    public Quiz saveQuiz(Quiz quiz, User creator) {
        quiz.setCreatedBy(creator);
        return quizRepository.save(quiz);
    }
    
    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }
    
    // Quiz Attempt methods
    public QuizAttempt startQuizAttempt(Quiz quiz, User student) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setStartTime(LocalDateTime.now());
        return quizAttemptRepository.save(attempt);
    }
    
    public Optional<QuizAttempt> findAttemptByIdWithDetails(Long attemptId) {
        return quizAttemptRepository.findByIdAndFetchQuizWithQuestions(attemptId);
    }
    
    @Transactional
    public QuizAttempt completeQuizAttempt(Long attemptId, Map<Long, String> answers) {
        Optional<QuizAttempt> attemptOpt = findAttemptByIdWithDetails(attemptId);
        if (attemptOpt.isEmpty()) {
            throw new IllegalArgumentException("Quiz attempt with ID " + attemptId + " not found.");
        }
        QuizAttempt attempt = attemptOpt.get();
        
        Quiz quiz = attempt.getQuiz();
        if (quiz == null) {
             throw new IllegalStateException("Quiz attempt " + attemptId + " is not associated with a quiz.");
        }
        
        List<QuizQuestion> questions = quiz.getQuestions();
        if (questions == null) {
             throw new IllegalStateException("Quiz " + quiz.getId() + " has no questions associated with it.");
        }
        
        int totalQuestions = questions.size();
        int correctAnswers = 0;
        
        for (QuizQuestion question : questions) {
            String studentAnswer = answers.get(question.getId());
            if (studentAnswer != null && studentAnswer.equals(question.getCorrectAnswer())) {
                correctAnswers++;
            }
        }
        
        int score = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        
        attempt.setScore(score);
        attempt.setCompletionTime(LocalDateTime.now());
        attempt.setCompleted(true);
        
        return quizAttemptRepository.save(attempt);
    }
    
    public List<QuizAttempt> findAttemptsByQuiz(Quiz quiz) {
        return quizAttemptRepository.findByQuiz(quiz);
    }
    
    public List<QuizAttempt> findAttemptsByStudent(User student) {
        return quizAttemptRepository.findByStudent(student);
    }
    
    public boolean hasStudentCompletedQuiz(Quiz quiz, User student) {
        return quizAttemptRepository.existsByQuizAndStudentAndCompletedTrue(quiz, student);
    }

    // Find active quizzes, optionally filtered by subject
    public List<Quiz> findActiveQuizzesBySubject(String subject) {
        if (subject == null || subject.trim().isEmpty() || subject.equals("All Subjects")) {
            return quizRepository.findByIsActiveTrue(); // Fetch all active if no subject or 'All' selected
        } else {
            return quizRepository.findActiveQuizzesBySubject(subject);
        }
    }
} 