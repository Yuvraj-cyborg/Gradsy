package com.example.application.views.teacher;

import com.example.application.data.Note;
import com.example.application.data.User;
import com.example.application.security.SecurityService;
import com.example.application.services.NoteService;
import com.example.application.services.UserService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

@Route(value = "teacher/notes/editor", layout = MainLayout.class)
@PageTitle("Note Editor | Learning Management System")
@RolesAllowed({"TEACHER"})
public class NoteEditorView extends VerticalLayout implements HasUrlParameter<Long> {

    private final SecurityService securityService;
    private final UserService userService;
    private final NoteService noteService;
    
    private User currentUser;
    private Note note;
    private boolean isEditMode = false;
    
    private TextField titleField;
    private TextArea contentField;
    
    @Autowired
    public NoteEditorView(
            SecurityService securityService,
            UserService userService,
            NoteService noteService) {
        this.securityService = securityService;
        this.userService = userService;
        this.noteService = noteService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        // Get current user
        String username = securityService.getAuthenticatedUser().getUsername();
        currentUser = userService.findUserByUsername(username);
    }
    
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long noteId) {
        removeAll();
        
        if (noteId != null) {
            // Edit existing note
            isEditMode = true;
            
            // Find the note
            Optional<Note> optionalNote = noteService.findById(noteId);
            if (optionalNote.isPresent()) {
                note = optionalNote.get();
                // Security check - only allow the owner to edit
                if (!note.getUser().equals(currentUser)) {
                    showUnauthorizedMessage();
                    return;
                }
                
                // Build edit form
                buildNoteForm(true);
                populateForm();
            } else {
                add(new H2("Note not found"));
                Button backButton = new Button("Back to Dashboard", e -> UI.getCurrent().navigate(TeacherDashboardView.class));
                add(backButton);
            }
        } else {
            // Create new note
            isEditMode = false;
            note = new Note();
            note.setUser(currentUser);
            
            buildNoteForm(false);
        }
    }
    
    private void buildNoteForm(boolean isEdit) {
        H2 title = new H2(isEdit ? "Edit Note" : "Add Note");
        
        FormLayout form = new FormLayout();
        form.setWidth("100%");
        form.setMaxWidth("800px");
        
        titleField = new TextField("Title");
        titleField.setWidth("100%");
        titleField.setRequired(true);
        
        contentField = new TextArea("Content");
        contentField.setWidth("100%");
        contentField.setMinHeight("300px");
        
        form.add(titleField, contentField);
        
        Button saveButton = new Button(isEdit ? "Update" : "Save", e -> saveNote());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("Cancel", e -> UI.getCurrent().navigate(TeacherDashboardView.class));
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);
        
        add(title, form, buttonLayout);
    }
    
    private void populateForm() {
        titleField.setValue(note.getTitle() != null ? note.getTitle() : "");
        contentField.setValue(note.getContent() != null ? note.getContent() : "");
    }
    
    private void saveNote() {
        if (titleField.getValue().trim().isEmpty()) {
            Notification.show("Title is required");
            return;
        }
        
        note.setTitle(titleField.getValue().trim());
        note.setContent(contentField.getValue());
        
        try {
            noteService.saveNote(note);
            Notification.show("Note " + (isEditMode ? "updated" : "added") + " successfully");
            UI.getCurrent().navigate(TeacherDashboardView.class);
        } catch (Exception ex) {
            Notification.show("Error saving note: " + ex.getMessage());
        }
    }
    
    private void showUnauthorizedMessage() {
        removeAll();
        add(new H2("Unauthorized Access"));
        add(new Button("Back to Dashboard", e -> UI.getCurrent().navigate(TeacherDashboardView.class)));
    }
} 