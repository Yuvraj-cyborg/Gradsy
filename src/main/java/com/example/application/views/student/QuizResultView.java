package com.example.application.views.student;

import com.example.application.data.Quiz;
import com.example.application.data.QuizAttempt;
import com.example.application.data.QuizQuestion;
import com.example.application.data.User;
import com.example.application.security.SecurityService;
import com.example.application.services.QuizService;
import com.example.application.services.UserService;
import com.example.application.views.MainLayout;
import com.example.application.views.teacher.TeacherDashboardView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Route(value = "quiz-result", layout = MainLayout.class)
@PageTitle("Quiz Results | Learning Management System")
@RolesAllowed({"STUDENT", "TEACHER"})
public class QuizResultView extends VerticalLayout implements HasUrlParameter<Long> {

    private final SecurityService securityService;
    private final UserService userService;
    private final QuizService quizService;
    
    private User currentUser;
    private QuizAttempt attempt;
    private Quiz quiz;
    private List<QuizQuestion> questions;
    private Map<Long, String> studentAnswers;

    @Autowired
    public QuizResultView(
            SecurityService securityService,
            UserService userService,
            QuizService quizService) {
        this.securityService = securityService;
        this.userService = userService;
        this.quizService = quizService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        // Get current user
        String username = securityService.getAuthenticatedUser().getUsername();
        currentUser = userService.findUserByUsername(username);
    }

    @Override
    public void setParameter(BeforeEvent event, Long attemptId) {
        removeAll(); // Clear previous content

        // Ensure currentUser is resolved here if not in constructor
        if (currentUser == null) {
            try {
                 String username = securityService.getAuthenticatedUser().getUsername();
                 currentUser = userService.findUserByUsername(username);
                 if (currentUser == null) throw new IllegalStateException("User not found");
            } catch (Exception e) {
                 showErrorMessage("Authentication Error", "Could not identify current user.");
                 return;
            }
        }

        // Find the attempt using the service method that fetches details
        Optional<QuizAttempt> attemptOpt = quizService.findAttemptByIdWithDetails(attemptId);
        
        if (attemptOpt.isPresent()) {
            attempt = attemptOpt.get();
            quiz = attempt.getQuiz(); // Quiz is already fetched
            
            // Get the student who took the attempt
            User student = attempt.getStudent();
            if (student == null) { 
                showErrorMessage("Attempt Error", "This attempt is not linked to a student.");
                return;
            }
            
            // Get the teacher who created the quiz
            if (quiz == null) { 
                showErrorMessage("Attempt Error", "This attempt is not linked to a quiz.");
                return;
            }
            User teacher = quiz.getCreatedBy();
            if (teacher == null) { 
                showErrorMessage("Quiz Error", "This quiz does not have an assigned creator.");
                return;
            }

            // Security check: Allow access if current user is the student 
            // OR if the current user is the teacher who created the quiz.
            if (!currentUser.equals(student) && !currentUser.equals(teacher)) {
                showUnauthorizedMessage();
                return;
            }
            
            // Check if attempt is completed
            if (!attempt.isCompleted()) {
                showIncompleteMessage();
                return;
            }
            
            // Load questions - Already fetched with the attempt
            if (quiz.getQuestions() == null) {
                showErrorMessage("Quiz Error", "Could not load questions for this quiz.");
                return;
            }
            questions = new ArrayList<>(quiz.getQuestions());
            
            // Create a mock map of student answers for now
            // TODO: In a real implementation, this should load saved answers
            studentAnswers = new HashMap<>();
            for (QuizQuestion question : questions) {
                studentAnswers.put(question.getId(), question.getCorrectAnswer()); // Mock data!
            }
            
            // Build UI
            buildResultsUI();
        } else {
            showNotFoundMessage();
        }
    }
    
    private void buildResultsUI() {
        // Results header
        H2 title = new H2("Quiz Results: " + quiz.getTitle());
        
        // Summary section
        VerticalLayout summaryLayout = new VerticalLayout();
        summaryLayout.setClassName("quiz-summary");
        
        H3 summaryTitle = new H3("Summary");
        
        Span scoreSpan = new Span("Score: " + attempt.getScore() + "%");
        scoreSpan.getStyle().set("font-size", "1.2em");
        scoreSpan.getStyle().set("font-weight", "bold");
        
        Span completedSpan = new Span("Completed: " + 
                attempt.getCompletionTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        
        Span timeSpan = new Span("Time Taken: " + calculateTimeTaken());
        
        // Progress bar for visual score
        ProgressBar scoreBar = new ProgressBar(0, 100, attempt.getScore());
        scoreBar.setWidth("300px");
        
        if (attempt.getScore() >= 70) {
            scoreBar.getStyle().set("--lumo-primary-color", "green");
        } else if (attempt.getScore() >= 50) {
            scoreBar.getStyle().set("--lumo-primary-color", "orange");
        } else {
            scoreBar.getStyle().set("--lumo-primary-color", "red");
        }
        
        summaryLayout.add(summaryTitle, scoreSpan, scoreBar, completedSpan, timeSpan);
        
        // Questions and answers section
        VerticalLayout questionsLayout = new VerticalLayout();
        questionsLayout.setClassName("questions-answers");
        
        H3 questionsTitle = new H3("Questions and Answers");
        questionsLayout.add(questionsTitle);
        
        // Add each question with student's answer
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            questionsLayout.add(createQuestionResultView(i + 1, question));
        }
        
        // Back to dashboard button
        Button backButton = new Button("Back to Dashboard", e -> UI.getCurrent().navigate(StudentDashboardView.class));
        backButton.getStyle().set("margin-top", "20px");
        
        add(title, summaryLayout, questionsLayout, backButton);
    }
    
    private String calculateTimeTaken() {
        long minutes = java.time.Duration.between(
                attempt.getStartTime(), attempt.getCompletionTime()).toMinutes();
        
        return minutes + " minute" + (minutes != 1 ? "s" : "");
    }
    
    private Div createQuestionResultView(int questionNumber, QuizQuestion question) {
        Div questionDiv = new Div();
        questionDiv.setClassName("question-result");
        questionDiv.getStyle().set("border", "1px solid #ddd");
        questionDiv.getStyle().set("border-radius", "5px");
        questionDiv.getStyle().set("padding", "15px");
        questionDiv.getStyle().set("margin-bottom", "15px");
        
        // Question header
        H4 questionText = new H4(questionNumber + ". " + question.getQuestionText());
        
        // Student's answer
        String studentAnswer = studentAnswers.getOrDefault(question.getId(), "Not answered");
        
        Paragraph yourAnswerPara = new Paragraph("Your answer: " + studentAnswer);
        
        // Correct answer
        Paragraph correctAnswerPara = new Paragraph("Correct answer: " + question.getCorrectAnswer());
        
        // Result icon
        Icon resultIcon;
        if (studentAnswer.equals(question.getCorrectAnswer())) {
            resultIcon = new Icon(VaadinIcon.CHECK);
            resultIcon.getStyle().set("color", "green");
            questionDiv.getStyle().set("border-left", "5px solid green");
        } else {
            resultIcon = new Icon(VaadinIcon.CLOSE);
            resultIcon.getStyle().set("color", "red");
            questionDiv.getStyle().set("border-left", "5px solid red");
        }
        
        HorizontalLayout questionHeader = new HorizontalLayout(resultIcon, questionText);
        questionHeader.setAlignItems(Alignment.CENTER);
        
        questionDiv.add(questionHeader, yourAnswerPara, correctAnswerPara);
        return questionDiv;
    }
    
    private void showNotFoundMessage() {
        removeAll();
        add(new H2("Not Found"));
        add(new Paragraph("The requested quiz attempt could not be found."));
        add(new Button("Back to Dashboard", e -> navigateBack()));
    }

    private void showUnauthorizedMessage() {
        removeAll();
        add(new H2("Unauthorized Access"));
        add(new Paragraph("You are not authorized to view these quiz results."));
        add(new Button("Back to Dashboard", e -> navigateBack()));
    }
    
    private void showIncompleteMessage() {
        removeAll();
        add(new H2("Quiz Not Completed"));
        add(new Paragraph("This quiz attempt has not been completed yet."));
        add(new Button("Back to Dashboard", e -> navigateBack()));
    }

    private void showErrorMessage(String title, String message) {
        removeAll();
        add(new H2(title));
        add(new Paragraph(message));
        add(new Button("Back to Dashboard", e -> navigateBack()));
    }
    
    // Helper to navigate back to the correct dashboard based on the *current* user's role
    private void navigateBack() {
        Optional<UserDetails> userDetailsOpt = securityService.getAuthenticatedUserOptional();
        boolean isTeacher = userDetailsOpt
            .map(UserDetails::getAuthorities)
            .map(authorities -> authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER")))
            .orElse(false);
            
        if (isTeacher) {
            UI.getCurrent().navigate(TeacherDashboardView.class);
        } else {
            // Default to student dashboard if not teacher or if user details not found
            UI.getCurrent().navigate(StudentDashboardView.class);
        }
    }
} 