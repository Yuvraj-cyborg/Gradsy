package com.example.application.views.teacher;

import com.example.application.data.Quiz;
import com.example.application.data.User;
import com.example.application.security.SecurityService;
import com.example.application.services.QuizService;
import com.example.application.services.UserService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "teacher/quiz/editor", layout = MainLayout.class)
@PageTitle("Quiz Editor | Learning Management System")
@RolesAllowed({"TEACHER"})
public class QuizEditorView extends VerticalLayout implements HasUrlParameter<Long> {

    private final SecurityService securityService;
    private final UserService userService;
    private final QuizService quizService;

    private User currentUser;
    private Quiz quiz;
    private boolean isEditMode = false;

    private TextField titleField;
    private TextArea descriptionField;
    private IntegerField durationField;
    // TODO: Add components for managing questions (e.g., a Grid or custom component)

    @Autowired
    public QuizEditorView(
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
    public void setParameter(BeforeEvent event, @OptionalParameter Long quizId) {
        removeAll(); // Clear previous content if reusing the view

        if (quizId != null) {
            // Edit existing quiz
            isEditMode = true;
            Optional<Quiz> quizOpt = Optional.ofNullable(quizService.findById(quizId)); // Assuming findById returns Quiz or null
            if (quizOpt.isPresent()) {
                quiz = quizOpt.get();

                // Security check - only allow the owner to edit (or admins if applicable)
                if (!quiz.getCreatedBy().equals(currentUser)) {
                    showUnauthorizedMessage();
                    return;
                }

                buildQuizForm(true);
                populateForm();
            } else {
                showNotFoundMessage();
            }
        } else {
            // Create new quiz
            isEditMode = false;
            quiz = new Quiz();
            quiz.setCreatedBy(currentUser); // Set creator immediately
            buildQuizForm(false);
        }
    }

    private void buildQuizForm(boolean isEdit) {
        H2 title = new H2(isEdit ? "Edit Quiz" : "Create New Quiz");

        FormLayout form = new FormLayout();
        form.setWidth("100%");
        form.setMaxWidth("800px");

        titleField = new TextField("Quiz Title");
        titleField.setWidth("100%");
        titleField.setRequired(true);

        descriptionField = new TextArea("Description");
        descriptionField.setWidth("100%");

        durationField = new IntegerField("Duration (minutes)");
        durationField.setStepButtonsVisible(true);
        durationField.setMin(1);

        // --- Placeholder for Question Management ---
        VerticalLayout questionsSection = new VerticalLayout();
        questionsSection.add(new H3("Questions"));
        questionsSection.add(new Paragraph("Question management UI will go here..."));
        // --- End Placeholder ---

        form.add(titleField, descriptionField, durationField);
        form.setColspan(descriptionField, 2);

        Button saveButton = new Button(isEdit ? "Update Quiz" : "Save Quiz", e -> saveQuiz());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> UI.getCurrent().navigate(TeacherDashboardView.class));

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        add(title, form, questionsSection, buttonLayout);
    }

    private void populateForm() {
        if (quiz != null) {
            titleField.setValue(quiz.getTitle() != null ? quiz.getTitle() : "");
            descriptionField.setValue(quiz.getDescription() != null ? quiz.getDescription() : "");
            durationField.setValue(quiz.getDurationMinutes() != null ? quiz.getDurationMinutes() : 0);
            // TODO: Populate question management component
        }
    }

    private void saveQuiz() {
        if (titleField.getValue().trim().isEmpty()) {
            Notification.show("Quiz Title is required", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
         if (durationField.getValue() == null || durationField.getValue() <= 0) {
            Notification.show("Duration must be a positive number of minutes", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        // TODO: Add validation for questions

        quiz.setTitle(titleField.getValue().trim());
        quiz.setDescription(descriptionField.getValue());
        quiz.setDurationMinutes(durationField.getValue());
        // TODO: Get questions from the question management component and set them on the quiz

        try {
            quizService.saveQuiz(quiz, currentUser); // Pass creator again? Service method might handle it
            Notification.show("Quiz " + (isEditMode ? "updated" : "saved") + " successfully.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            // Navigate back to dashboard
            UI.getCurrent().navigate(TeacherDashboardView.class);
        } catch (Exception ex) {
            ex.printStackTrace(); // Log exception
            Notification.show("Error saving quiz: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showUnauthorizedMessage() {
        removeAll();
        add(new H2("Unauthorized Access"));
        add(new Paragraph("You are not authorized to edit this quiz."));
        add(new Button("Back to Dashboard", e -> UI.getCurrent().navigate(TeacherDashboardView.class)));
    }

    private void showNotFoundMessage() {
         removeAll();
        add(new H2("Not Found"));
        add(new Paragraph("The requested quiz could not be found."));
        add(new Button("Back to Dashboard", e -> UI.getCurrent().navigate(TeacherDashboardView.class)));
    }
} 