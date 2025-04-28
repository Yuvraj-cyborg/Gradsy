package com.example.application.views.student;

import com.example.application.data.Quiz;
import com.example.application.data.QuizAttempt;
import com.example.application.data.QuizQuestion;
import com.example.application.data.User;
import com.example.application.security.SecurityService;
import com.example.application.services.QuizService;
import com.example.application.services.UserService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Route(value = "quiz", layout = MainLayout.class)
@PageTitle("Take Quiz | Learning Management System")
@RolesAllowed({"STUDENT"})
public class QuizView extends VerticalLayout implements HasUrlParameter<Long> {

    private final SecurityService securityService;
    private final UserService userService;
    private final QuizService quizService;
    
    private User currentUser;
    private Quiz quiz;
    private QuizAttempt currentAttempt;
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    
    private VerticalLayout questionContainer;
    private Span questionCountLabel;
    private ProgressBar progressBar;
    private Button prevButton;
    private Button nextButton;
    private Button submitButton;
    
    // Store student answers
    private Map<Long, String> studentAnswers = new HashMap<>();

    @Autowired
    public QuizView(
            SecurityService securityService,
            UserService userService,
            QuizService quizService) {
        this.securityService = securityService;
        this.userService = userService;
        this.quizService = quizService;
        
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        
        // Get current user
        String username = securityService.getAuthenticatedUser().getUsername();
        currentUser = userService.findUserByUsername(username);
    }

    @Override
    public void setParameter(BeforeEvent event, Long quizId) {
        removeAll(); // Clear previous content

        // Use the new service method to fetch quiz with questions
        Optional<Quiz> quizOpt = quizService.findByIdWithQuestions(quizId); 

        if (quizOpt.isPresent()) {
            quiz = quizOpt.get();

            // Check if the quiz is active
            if (!quiz.isActive()) {
                showQuizNotActiveMessage();
                return;
            }

            // Load questions safely - Already fetched now!
            if (quiz.getQuestions() == null) { 
                 // This check might still be useful if a quiz can be saved with null questions
                 Notification.show("Error: Quiz has no questions.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 add(new Button("Back to Dashboard", e -> UI.getCurrent().navigate(StudentDashboardView.class)));
                 return;
            }
            questions = new ArrayList<>(quiz.getQuestions()); // Should not throw LazyInitializationException now
            
            // Check if there are any questions
            if (questions.isEmpty()) {
                Notification.show("Error: Quiz has no questions.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 add(new Button("Back to Dashboard", e -> UI.getCurrent().navigate(StudentDashboardView.class)));
                 return;
            }
            
            // Check completion status safely
            boolean hasCompleted = false;
            try {
                 List<QuizAttempt> studentAttempts = quizService.findAttemptsByStudent(currentUser);
                 if (studentAttempts != null) { // Check if list itself is null (defensive)
                     for (QuizAttempt attempt : studentAttempts) {
                        // Check if attempt or its quiz is null (defensive)
                        if (attempt != null && attempt.getQuiz() != null && attempt.getQuiz().equals(quiz) && attempt.isCompleted()) { 
                            hasCompleted = true;
                            break;
                        }
                    }
                 }
            } catch (Exception e) {
                // Log error potentially related to fetching attempts
                System.err.println("Error checking quiz completion status: " + e.getMessage());
                Notification.show("Error checking previous attempts.", 3000, Notification.Position.MIDDLE)
                           .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return; // Prevent proceeding if check fails
            }
            
            if (hasCompleted) {
                showQuizCompletedMessage();
                return;
            }
            
            // Start new attempt logic...
            // Create and save the attempt immediately to get an ID
            try {
                currentAttempt = quizService.startQuizAttempt(quiz, currentUser); 
            } catch (Exception e) {
                 // Handle potential errors during attempt creation (e.g., database issues)
                 System.err.println("Error starting quiz attempt: " + e.getMessage());
                 Notification.show("Error starting quiz. Please try again.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 add(new Button("Back to Dashboard", ev -> UI.getCurrent().navigate(StudentDashboardView.class)));
                 return; // Stop processing if attempt can't be created
            }
            
            // Check if attempt creation was successful and we have an ID
            if (currentAttempt == null || currentAttempt.getId() == null) {
                 Notification.show("Error starting quiz: Could not create attempt record.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 add(new Button("Back to Dashboard", ev -> UI.getCurrent().navigate(StudentDashboardView.class)));
                 return; 
            }
            
            studentAnswers = new HashMap<>(); // Reset answers for the new attempt
            
            buildQuizUI();
            showQuestion(currentQuestionIndex);

        } else {
            showQuizNotFoundMessage();
        }
    }
    
    private void buildQuizUI() {
        removeAll();
        
        // Quiz header
        H2 title = new H2(quiz.getTitle());
        Paragraph description = new Paragraph(quiz.getDescription());
        Span timeLimit = new Span("Time Limit: " + quiz.getDurationMinutes() + " minutes");
        
        // Progress tracking
        questionCountLabel = new Span("Question 1 of " + questions.size());
        progressBar = new ProgressBar(0, questions.size(), 1);
        progressBar.setWidth("50%");
        
        HorizontalLayout progressLayout = new HorizontalLayout(questionCountLabel, progressBar);
        progressLayout.setWidthFull();
        progressLayout.setAlignItems(Alignment.CENTER);
        
        // Question container
        questionContainer = new VerticalLayout();
        questionContainer.setWidthFull();
        questionContainer.setAlignItems(Alignment.START);
        
        // Navigation buttons
        prevButton = new Button("Previous", e -> showPreviousQuestion());
        prevButton.setEnabled(false);
        
        nextButton = new Button("Next", e -> showNextQuestion());
        
        submitButton = new Button("Submit Quiz", e -> submitQuiz());
        submitButton.setVisible(false);
        
        HorizontalLayout navigationButtons = new HorizontalLayout(prevButton, nextButton, submitButton);
        
        add(title, description, timeLimit, progressLayout, questionContainer, navigationButtons);
    }
    
    private void showQuestion(int index) {
        questionContainer.removeAll();
        
        QuizQuestion question = questions.get(index);
        
        // Update progress indicators
        questionCountLabel.setText("Question " + (index + 1) + " of " + questions.size());
        progressBar.setValue(index + 1);
        
        // Update navigation buttons
        prevButton.setEnabled(index > 0);
        nextButton.setVisible(index < questions.size() - 1);
        submitButton.setVisible(index == questions.size() - 1);
        
        // Display question
        H3 questionText = new H3((index + 1) + ". " + question.getQuestionText());
        questionContainer.add(questionText);
        
        // For multiple choice questions
        if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
            // Create some mock answers
            // In a real implementation, these would come from the database
            List<String> choices = getMockAnswerChoices(question);
            
            RadioButtonGroup<String> options = new RadioButtonGroup<>();
            options.setItems(choices);
            
            // If already answered, select the previous answer
            if (studentAnswers.containsKey(question.getId())) {
                options.setValue(studentAnswers.get(question.getId()));
            }
            
            // Save answer when selected
            options.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    studentAnswers.put(question.getId(), event.getValue());
                }
            });
            
            questionContainer.add(options);
        }
    }
    
    private List<String> getMockAnswerChoices(QuizQuestion question) {
        // In a real application, these would come from the database
        // For now, we'll create some mock choices including the correct answer
        
        if (question.getCorrectAnswer() != null) {
            // Create a list with the correct answer and some fake options
            return Arrays.asList(
                question.getCorrectAnswer(),
                "Wrong option 1",
                "Wrong option 2",
                "Wrong option 3"
            );
        }
        
        // Fallback if no correct answer
        return Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
    }
    
    private void showPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            showQuestion(currentQuestionIndex);
        }
    }
    
    private void showNextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }
    }
    
    private void submitQuiz() {
        // Check for unanswered questions
        if (studentAnswers.size() < questions.size()) {
            Notification.show("Please answer all questions before submitting.");
            return;
        }
        
        // Ensure we have a valid attempt ID
        if (currentAttempt == null || currentAttempt.getId() == null) {
             Notification.show("Error: Quiz attempt data is missing. Cannot submit.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
             return; 
        }
        
        try {
            // Call service to calculate score, save answers, and mark as complete
            quizService.completeQuizAttempt(currentAttempt.getId(), studentAnswers);
            
            // Navigate to results using the valid attempt ID
            Notification.show("Quiz submitted successfully!");
            UI.getCurrent().navigate("quiz-result/" + currentAttempt.getId());

        } catch (Exception e) {
            // Handle potential errors during submission (e.g., database issues)
            System.err.println("Error submitting quiz attempt: " + e.getMessage());
            Notification.show("Error submitting quiz. Please try again.", 5000, Notification.Position.MIDDLE)
                       .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void showQuizNotFoundMessage() {
        removeAll();
        add(new H2("Quiz Not Found"));
        add(new Paragraph("The requested quiz could not be found."));
        add(new Button("Back to Dashboard", e -> UI.getCurrent().navigate(StudentDashboardView.class)));
    }
    
    private void showQuizNotActiveMessage() {
        removeAll();
        add(new H2("Quiz Not Active"));
        add(new Paragraph("This quiz is currently not available."));
        add(new Button("Back to Dashboard", e -> UI.getCurrent().navigate(StudentDashboardView.class)));
    }

    private void showQuizCompletedMessage() {
        removeAll();
        add(new H2("Quiz Already Completed"));
        add(new Paragraph("You have already completed this quiz."));
        // Optionally add a button to view results if not already obvious
        // Button viewResultsButton = new Button("View Results", e -> /* find attempt ID and navigate */);
        add(new Button("Back to Dashboard", e -> UI.getCurrent().navigate(StudentDashboardView.class)));
    }
} 