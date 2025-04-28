package com.example.application.views.student;

import com.example.application.data.LearningMaterial;
import com.example.application.data.Quiz;
import com.example.application.data.StudentProfile;
import com.example.application.data.User;
import com.example.application.data.QuizAttempt;
import com.example.application.security.SecurityService;
import com.example.application.services.LearningMaterialService;
import com.example.application.services.QuizService;
import com.example.application.services.UserService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route(value = "student", layout = MainLayout.class)
@PageTitle("Student Dashboard | Learning Management System")
@RolesAllowed({"STUDENT"})
public class StudentDashboardView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserService userService;
    private final LearningMaterialService materialService;
    private final QuizService quizService;
    
    private StudentProfile studentProfile;
    private Grid<LearningMaterial> materialsGrid = new Grid<>(LearningMaterial.class);
    private Grid<Quiz> quizzesGrid = new Grid<>(Quiz.class);
    private Grid<QuizAttempt> attemptsGrid = new Grid<>(QuizAttempt.class);
    private ComboBox<String> subjectFilterComboBox;

    @Autowired
    public StudentDashboardView(
            SecurityService securityService,
            UserService userService,
            LearningMaterialService materialService,
            QuizService quizService) {
        this.securityService = securityService;
        this.userService = userService;
        this.materialService = materialService;
        this.quizService = quizService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        String username = securityService.getAuthenticatedUser().getUsername();
        User currentUser = userService.findUserByUsername(username);
        studentProfile = userService.findStudentByUser(currentUser);

        // Create ComboBox first as other sections might depend on its value
        subjectFilterComboBox = createSubjectFilterComboBox();

        add(
            createHeaderSection(),
            subjectFilterComboBox, // Add ComboBox to the layout
            createLearningMaterialsSection(),
            createQuizzesSection(),
            createQuizAttemptsSection()
        );
        
        // Initial data load
        refreshMaterialsGrid();
        refreshQuizzesGrid();
        refreshAttemptsGrid();
    }

    private ComboBox<String> createSubjectFilterComboBox() {
        ComboBox<String> comboBox = new ComboBox<>("Filter by Subject");
        List<String> subjects = new ArrayList<>();
        subjects.add("All Subjects"); // Add option to show all
        subjects.addAll(userService.findAllDistinctSubjectAreas());
        comboBox.setItems(subjects);
        comboBox.setValue("All Subjects"); // Default value
        
        // Add listener to refresh grids when selection changes
        comboBox.addValueChangeListener(event -> {
            refreshMaterialsGrid();
            refreshQuizzesGrid();
        });
        return comboBox;
    }

    private Component createHeaderSection() {
        VerticalLayout header = new VerticalLayout();
        header.setAlignItems(Alignment.START);
        
        H2 title = new H2("Student Dashboard");
        H3 welcome = new H3("Welcome, " + studentProfile.getFirstName() + " " + studentProfile.getLastName());
        
        header.add(title, welcome);
        return header;
    }

    private Component createLearningMaterialsSection() {
        VerticalLayout layout = new VerticalLayout();
        H3 sectionTitle = new H3("Learning Materials");
        
        materialsGrid.removeAllColumns();
        materialsGrid.addColumn(LearningMaterial::getTitle).setHeader("Title");
        materialsGrid.addColumn(LearningMaterial::getDescription).setHeader("Description");
        materialsGrid.addColumn(material -> material.getUploadedBy().getUsername()).setHeader("Teacher");
        materialsGrid.addColumn(LearningMaterial::getCreatedAt).setHeader("Uploaded Date");
        
        Button downloadButton = new Button("Download", e -> {
            LearningMaterial selected = materialsGrid.asSingleSelect().getValue();
            if (selected != null && selected.getFilePath() != null) {
                // Create an Anchor for downloading
                String filePath = selected.getFilePath();
                Anchor downloadLink = new Anchor("/download/" + filePath, "Download");
                downloadLink.getElement().setAttribute("download", true);
                downloadLink.getElement().setAttribute("target", "_blank");
                
                // Open in a new tab
                getUI().ifPresent(ui -> ui.getPage().open("/download/" + filePath, "_blank"));
                Notification.show("Downloading material...");
            } else {
                Notification.show("No file available for download");
            }
        });
        
        layout.add(sectionTitle, materialsGrid, downloadButton);
        return layout;
    }

    private void refreshMaterialsGrid() {
        String selectedSubject = subjectFilterComboBox.getValue();
        materialsGrid.setItems(materialService.findMaterialsBySubject(selectedSubject));
    }

    private Component createQuizzesSection() {
        VerticalLayout layout = new VerticalLayout();
        H3 sectionTitle = new H3("Available Quizzes");
        
        quizzesGrid.removeAllColumns();
        quizzesGrid.addColumn(Quiz::getTitle).setHeader("Title");
        quizzesGrid.addColumn(Quiz::getDescription).setHeader("Description");
        quizzesGrid.addColumn(quiz -> quiz.getCreatedBy().getUsername()).setHeader("Created By");
        quizzesGrid.addColumn(Quiz::getDurationMinutes).setHeader("Duration (minutes)");
        
        Button startQuizButton = new Button("Start Quiz", e -> {
            Quiz selected = quizzesGrid.asSingleSelect().getValue();
            if (selected != null) {
                // Navigate to quiz view with ID parameter
                getUI().ifPresent(ui -> ui.navigate("quiz/" + selected.getId()));
            }
        });
        
        layout.add(sectionTitle, quizzesGrid, startQuizButton);
        return layout;
    }

    private void refreshQuizzesGrid() {
        String selectedSubject = subjectFilterComboBox.getValue();
        quizzesGrid.setItems(quizService.findActiveQuizzesBySubject(selectedSubject));
    }

    private Component createQuizAttemptsSection() {
        VerticalLayout layout = new VerticalLayout();
        H3 sectionTitle = new H3("My Quiz Attempts");
        
        attemptsGrid.removeAllColumns();
        attemptsGrid.addColumn(attempt -> attempt.getQuiz().getTitle()).setHeader("Quiz");
        attemptsGrid.addColumn(QuizAttempt::getStartTime).setHeader("Started At");
        attemptsGrid.addColumn(QuizAttempt::getCompletionTime).setHeader("Completed At");
        attemptsGrid.addColumn(QuizAttempt::getScore).setHeader("Score");
        attemptsGrid.addColumn(QuizAttempt::isCompleted).setHeader("Completed");
        
        Button viewResultButton = new Button("View Results", e -> {
            QuizAttempt selected = attemptsGrid.asSingleSelect().getValue();
            if (selected != null && selected.isCompleted()) {
                // Navigate to results view with attempt ID
                getUI().ifPresent(ui -> ui.navigate("quiz-result/" + selected.getId()));
            }
        });
        
        layout.add(sectionTitle, attemptsGrid, viewResultButton);
        return layout;
    }

    private void refreshAttemptsGrid() {
        User currentUser = userService.findUserByUsername(securityService.getAuthenticatedUser().getUsername());
        attemptsGrid.setItems(quizService.findAttemptsByStudent(currentUser));
    }
} 