package com.example.application.views.teacher;

import com.example.application.data.LearningMaterial;
import com.example.application.data.User;
import com.example.application.security.SecurityService;
import com.example.application.services.LearningMaterialService;
import com.example.application.services.UserService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Optional;

@Route(value = "teacher/materials/editor", layout = MainLayout.class)
@PageTitle("Material Editor | Learning Management System")
@RolesAllowed({"TEACHER"})
public class MaterialEditorView extends VerticalLayout implements HasUrlParameter<Long> {

    private final SecurityService securityService;
    private final UserService userService;
    private final LearningMaterialService materialService;
    
    private User currentUser;
    private LearningMaterial material;
    private boolean isEditMode = false;
    
    private TextField titleField;
    private TextArea descriptionField;
    private MemoryBuffer buffer = new MemoryBuffer();
    private Upload upload;
    private Button saveButton;
    
    @Autowired
    public MaterialEditorView(
            SecurityService securityService,
            UserService userService,
            LearningMaterialService materialService) {
        this.securityService = securityService;
        this.userService = userService;
        this.materialService = materialService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        // Get current user
        String username = securityService.getAuthenticatedUser().getUsername();
        currentUser = userService.findUserByUsername(username);
    }
    
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long materialId) {
        removeAll();
        
        if (materialId != null) {
            // Edit existing material
            isEditMode = true;
            
            // Find the material
            Optional<LearningMaterial> materialOpt = materialService.findById(materialId);
            if (materialOpt.isPresent()) {
                material = materialOpt.get();
                
                // Security check - only allow the owner to edit
                if (!material.getUploadedBy().equals(currentUser)) {
                    showUnauthorizedMessage();
                    return;
                }
                
                // Build edit form
                buildMaterialForm(true);
                populateForm();
            } else {
                add(new H2("Material not found"));
                Button backButton = new Button("Back to Dashboard", e -> UI.getCurrent().navigate(TeacherDashboardView.class));
                add(backButton);
            }
        } else {
            // Create new material
            isEditMode = false;
            material = new LearningMaterial();
            material.setUploadedBy(currentUser);
            
            buildMaterialForm(false);
        }
    }
    
    private void buildMaterialForm(boolean isEdit) {
        H2 title = new H2(isEdit ? "Edit Learning Material" : "Add Learning Material");
        
        FormLayout form = new FormLayout();
        form.setWidth("100%");
        form.setMaxWidth("800px");
        
        titleField = new TextField("Title");
        titleField.setWidth("100%");
        titleField.setRequired(true);
        
        descriptionField = new TextArea("Description");
        descriptionField.setWidth("100%");
        descriptionField.setMinHeight("150px");
        
        upload = new Upload(buffer);
        upload.setAcceptedFileTypes(
            "application/pdf", ".pdf",
            "application/msword", ".doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx",
            "text/plain", ".txt"
        );
        upload.setMaxFiles(1);
        upload.setDropAllowed(true);
        upload.setWidth("100%");
        upload.setMaxFileSize(10 * 1024 * 1024); // 10MB
        
        if (isEdit) {
            upload.setDropLabel(new Span(material.getFilePath() != null ? 
                "Current file: " + material.getFilePath() + ". Drop to replace." : 
                "No file uploaded. Drop to add."));
        } else {
            upload.setDropLabel(new Span("Drop file here or click to upload"));
        }
        
        form.add(titleField, descriptionField, upload);
        
        saveButton = new Button(isEdit ? "Update" : "Save", e -> saveMaterial());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("Cancel", e -> UI.getCurrent().navigate(TeacherDashboardView.class));
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);
        
        add(title, form, buttonLayout);
    }
    
    private void populateForm() {
        titleField.setValue(material.getTitle() != null ? material.getTitle() : "");
        descriptionField.setValue(material.getDescription() != null ? material.getDescription() : "");
    }
    
    private void saveMaterial() {
        if (titleField.getValue().trim().isEmpty()) {
            Notification.show("Title is required");
            return;
        }
        
        material.setTitle(titleField.getValue().trim());
        material.setDescription(descriptionField.getValue());
        
        try {
            InputStream inputStream = null;
            String fileName = null;
            String mimeType = null;
            long contentLength = 0L;

            if (buffer.getFileData() != null && buffer.getFileName() != null && !buffer.getFileName().isEmpty()) {
                // Get file details from MemoryBuffer
                InputStream tempInputStream = buffer.getInputStream();
                byte[] fileBytes = tempInputStream.readAllBytes();
                tempInputStream.close();

                inputStream = new ByteArrayInputStream(fileBytes);
                fileName = buffer.getFileName();
                mimeType = buffer.getFileData().getMimeType();
                contentLength = fileBytes.length;
            }

            // Call the updated service method
            // Pass nulls for file details if no file was uploaded
            materialService.saveLearningMaterial(material, inputStream, fileName, mimeType, contentLength, currentUser);

            Notification.show("Material " + (isEditMode ? "updated" : "added") + " successfully.");

            // Navigate back to dashboard
            UI.getCurrent().navigate(TeacherDashboardView.class);

        } catch (Exception ex) {
            // Log the exception for debugging
            ex.printStackTrace(); // Print stack trace to console/log
            Notification.show("Error saving material: " + ex.getMessage() + " Please check logs.");
        }
    }
    
    private void showUnauthorizedMessage() {
        removeAll();
        add(new H2("Unauthorized Access"));
        add(new Button("Back to Dashboard", e -> UI.getCurrent().navigate(TeacherDashboardView.class)));
    }

    public Button getSaveButton() {
        return saveButton;
    }
    
    public void setBufferForTesting(MemoryBuffer buffer) {
        this.buffer = buffer;
    }

    public Upload getFileUpload() {
        return upload;
    }
} 