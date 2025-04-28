package com.example.application.views.teacher;

import com.example.application.data.LearningMaterial;
import com.example.application.data.Quiz;
import com.example.application.data.TeacherProfile;
import com.example.application.data.User;
import com.example.application.data.QuizAttempt;
import com.example.application.data.Note;
import com.example.application.security.SecurityService;
import com.example.application.services.LearningMaterialService;
import com.example.application.services.QuizService;
import com.example.application.services.UserService;
import com.example.application.services.NoteService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route(value = "teacher", layout = MainLayout.class)
@PageTitle("Teacher Dashboard | Learning Management System")
@RolesAllowed({"TEACHER"})
public class TeacherDashboardView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserService userService;
    private final LearningMaterialService materialService;
    private final QuizService quizService;
    private final NoteService noteService;
    
    private User currentUser;
    private TeacherProfile teacherProfile;
    private Grid<LearningMaterial> materialsGrid = new Grid<>(LearningMaterial.class);
    private Grid<Quiz> quizzesGrid = new Grid<>(Quiz.class);
    private Grid<QuizAttempt> studentAttemptsGrid = new Grid<>(QuizAttempt.class);
    private Grid<Note> notesGrid = new Grid<>(Note.class);

    @Autowired
    public TeacherDashboardView(
            SecurityService securityService,
            UserService userService,
            LearningMaterialService materialService,
            QuizService quizService,
            NoteService noteService) {
        this.securityService = securityService;
        this.userService = userService;
        this.materialService = materialService;
        this.quizService = quizService;
        this.noteService = noteService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        // Get current user
        String username = securityService.getAuthenticatedUser().getUsername();
        currentUser = userService.findUserByUsername(username);
        teacherProfile = userService.findTeacherByUser(currentUser);

        add(
            createHeaderSection(),
            createNotesSection(),
            createLearningMaterialsSection(),
            createQuizzesSection(),
            createStudentProgressSection()
        );
    }

    private Component createHeaderSection() {
        VerticalLayout header = new VerticalLayout();
        header.setAlignItems(Alignment.START);
        
        H2 title = new H2("Teacher Dashboard");
        H3 welcome = new H3("Welcome, " + teacherProfile.getFirstName() + " " + teacherProfile.getLastName());
        H3 subject = new H3("Subject: " + (teacherProfile.getSubjectArea() != null ? teacherProfile.getSubjectArea() : "Not specified"));
        
        header.add(title, welcome, subject);
        return header;
    }

    private Component createNotesSection() {
        VerticalLayout layout = new VerticalLayout();
        H3 sectionTitle = new H3("My Notes");
        
        // Configure grid
        notesGrid.removeAllColumns();
        notesGrid.addColumn(Note::getTitle).setHeader("Title");
        notesGrid.addColumn(Note::getContent).setHeader("Content");
        notesGrid.addColumn(Note::getCreatedAt).setHeader("Created At");
        notesGrid.addColumn(Note::getUpdatedAt).setHeader("Last Updated");
        
        notesGrid.setItems(noteService.findNotesByUser(currentUser));
        
        // Buttons for notes
        Button addButton = new Button("Add Note", e -> getUI().ifPresent(ui -> ui.navigate("teacher/notes/editor")));
        Button editButton = new Button("Edit Note", e -> {
            Note selected = notesGrid.asSingleSelect().getValue();
            if (selected != null) {
                getUI().ifPresent(ui -> ui.navigate("teacher/notes/editor/" + selected.getId()));
            }
        });
        Button deleteButton = new Button("Delete Note", e -> {
            Note selected = notesGrid.asSingleSelect().getValue();
            if (selected != null) {
                noteService.deleteNote(selected.getId());
                refreshNotesGrid();
                Notification.show("Note deleted");
            }
        });
        
        HorizontalLayout buttonsLayout = new HorizontalLayout(addButton, editButton, deleteButton);
        
        layout.add(sectionTitle, notesGrid, buttonsLayout);
        return layout;
    }

    private void refreshNotesGrid() {
        notesGrid.setItems(noteService.findNotesByUser(currentUser));
    }

    private Component createLearningMaterialsSection() {
        VerticalLayout layout = new VerticalLayout();
        H3 sectionTitle = new H3("My Learning Materials");
        
        // Configure grid
        materialsGrid.removeAllColumns();
        materialsGrid.addColumn(LearningMaterial::getTitle).setHeader("Title");
        materialsGrid.addColumn(LearningMaterial::getDescription).setHeader("Description");
        materialsGrid.addColumn(LearningMaterial::getCreatedAt).setHeader("Created At");
        materialsGrid.addColumn(material -> material.getFilePath() != null ? "Yes" : "No").setHeader("Has File");
        
        materialsGrid.setItems(materialService.findMaterialsByUser(currentUser));
        
        // Buttons for materials
        Button addButton = new Button("Add Material", e -> getUI().ifPresent(ui -> ui.navigate("teacher/material/new")));
        Button editButton = new Button("Edit", e -> {
            LearningMaterial selected = materialsGrid.asSingleSelect().getValue();
            if (selected != null) {
                getUI().ifPresent(ui -> ui.navigate("teacher/material/" + selected.getId()));
            }
        });
        Button deleteButton = new Button("Delete", e -> {
            LearningMaterial selected = materialsGrid.asSingleSelect().getValue();
            if (selected != null) {
                materialService.deleteMaterial(selected.getId());
                refreshMaterialsGrid();
                Notification.show("Material deleted");
            }
        });
        
        HorizontalLayout buttonsLayout = new HorizontalLayout(addButton, editButton, deleteButton);
        
        layout.add(sectionTitle, materialsGrid, buttonsLayout);
        return layout;
    }

    private void refreshMaterialsGrid() {
        materialsGrid.setItems(materialService.findMaterialsByUser(currentUser));
    }

    private Component createQuizzesSection() {
        VerticalLayout layout = new VerticalLayout();
        H3 sectionTitle = new H3("My Quizzes");
        
        // Configure grid
        quizzesGrid.removeAllColumns();
        quizzesGrid.addColumn(Quiz::getTitle).setHeader("Title");
        quizzesGrid.addColumn(Quiz::getDescription).setHeader("Description");
        quizzesGrid.addColumn(Quiz::getCreatedAt).setHeader("Created At");
        quizzesGrid.addColumn(Quiz::getDurationMinutes).setHeader("Duration (min)");
        quizzesGrid.addColumn(Quiz::isActive).setHeader("Active");
        
        quizzesGrid.setItems(quizService.findQuizzesByCreator(currentUser));
        
        // Buttons for quizzes
        Button addButton = new Button("Create Quiz", e -> getUI().ifPresent(ui -> ui.navigate("teacher/quiz/editor")));
        Button editButton = new Button("Edit Quiz", e -> {
            Quiz selected = quizzesGrid.asSingleSelect().getValue();
            if (selected != null) {
                getUI().ifPresent(ui -> ui.navigate("teacher/quiz/editor/" + selected.getId()));
            }
        });
        Button toggleActiveButton = new Button("Toggle Active", e -> {
            Quiz selected = quizzesGrid.asSingleSelect().getValue();
            if (selected != null) {
                selected.setActive(!selected.isActive());
                quizService.saveQuiz(selected, currentUser);
                refreshQuizzesGrid();
                Notification.show("Quiz " + (selected.isActive() ? "activated" : "deactivated"));
            }
        });
        Button deleteButton = new Button("Delete", e -> {
            Quiz selected = quizzesGrid.asSingleSelect().getValue();
            if (selected != null) {
                quizService.deleteQuiz(selected.getId());
                refreshQuizzesGrid();
                Notification.show("Quiz deleted");
            }
        });
        
        HorizontalLayout buttonsLayout = new HorizontalLayout(addButton, editButton, toggleActiveButton, deleteButton);
        
        layout.add(sectionTitle, quizzesGrid, buttonsLayout);
        return layout;
    }

    private void refreshQuizzesGrid() {
        quizzesGrid.setItems(quizService.findQuizzesByCreator(currentUser));
    }

    private Component createStudentProgressSection() {
        VerticalLayout layout = new VerticalLayout();
        H3 sectionTitle = new H3("Student Progress");
        
        // Configure grid
        studentAttemptsGrid.removeAllColumns();
        studentAttemptsGrid.addColumn(attempt -> attempt.getStudent().getUsername()).setHeader("Student");
        studentAttemptsGrid.addColumn(attempt -> attempt.getQuiz().getTitle()).setHeader("Quiz");
        studentAttemptsGrid.addColumn(QuizAttempt::getScore).setHeader("Score");
        studentAttemptsGrid.addColumn(QuizAttempt::getCompletionTime).setHeader("Completed At");
        studentAttemptsGrid.addColumn(QuizAttempt::isCompleted).setHeader("Completed");
        
        // Get all attempts for quizzes created by this teacher
        List<Quiz> teacherQuizzes = quizService.findQuizzesByCreator(currentUser);
        List<QuizAttempt> attempts = new ArrayList<>();
        
        // Get attempts for each quiz
        for (Quiz quiz : teacherQuizzes) {
            attempts.addAll(quizService.findAttemptsByQuiz(quiz));
        }
        
        studentAttemptsGrid.setItems(attempts);
        
        Button viewDetailsButton = new Button("View Details", e -> {
            QuizAttempt selected = studentAttemptsGrid.asSingleSelect().getValue();
            if (selected != null) {
                getUI().ifPresent(ui -> ui.navigate("quiz-result/" + selected.getId()));
            }
        });
        
        layout.add(sectionTitle, studentAttemptsGrid, viewDetailsButton);
        return layout;
    }
} 