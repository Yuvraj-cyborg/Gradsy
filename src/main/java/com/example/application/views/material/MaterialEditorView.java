package com.example.application.views.material;

import com.example.application.data.LearningMaterial;
import com.example.application.data.User;
import com.example.application.security.SecurityService;
import com.example.application.services.LearningMaterialService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Div;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

@PageTitle("Material Editor")
@Route(value = "materials/editor")
@PermitAll
public class MaterialEditorView extends VerticalLayout {

    private TextField title;
    private TextArea description;
    private Upload upload;
    private MemoryBuffer buffer;
    private Button saveButton;
    private Button cancelButton;
    private final LearningMaterialService materialService;
    private final SecurityService securityService;
    private LearningMaterial material;
    private User user;

    public MaterialEditorView(LearningMaterialService materialService, SecurityService securityService) {
        this.materialService = materialService;
        this.securityService = securityService;
        this.material = new LearningMaterial();
        
        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());
        
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    private Component createTitle() {
        return new H2("Material Editor");
    }

    private Component createFormLayout() {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidth("600px");
        
        title = new TextField("Title");
        title.setWidth("100%");
        title.setRequired(true);
        
        description = new TextArea("Description");
        description.setWidth("100%");
        description.setRequired(true);
        
        buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setWidth("100%");
        upload.setAcceptedFileTypes("application/pdf", ".pdf", 
                                   "application/msword", ".doc", 
                                   "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx",
                                   "image/jpeg", ".jpg", ".jpeg",
                                   "image/png", ".png");
        
        Div uploadLabel = new Div();
        uploadLabel.setText("Upload Learning Material");
        upload.setUploadButton(new Button("Choose File"));
        upload.setDropLabel(uploadLabel);
        
        formLayout.add(title, description, upload);
        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth("600px");
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        
        saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (isFormValid()) {
                saveMaterial();
            }
        });
        
        cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> clearForm());
        
        buttonLayout.add(cancelButton, saveButton);
        return buttonLayout;
    }

    public boolean isFormValid() {
        if (title.isEmpty()) {
            Notification.show("Please enter a title");
            return false;
        }
        if (description.isEmpty()) {
            Notification.show("Please enter a description");
            return false;
        }
        if (buffer.getFileName() == null || buffer.getFileName().isEmpty()) {
            Notification.show("Please upload a file");
            return false;
        }
        return true;
    }

    private void saveMaterial() {
        try {
            material.setTitle(title.getValue());
            material.setDescription(description.getValue());
            
            UserDetails userDetails = securityService.getAuthenticatedUser();
            user = findUserByUsername(userDetails.getUsername());
            
            InputStream inputStream = null;
            String fileName = null;
            String contentType = null;
            long fileSize = 0L;
            
            if (buffer.getFileData() != null && buffer.getFileName() != null && !buffer.getFileName().isEmpty()) {
                InputStream tempInputStream = buffer.getInputStream();
                byte[] fileBytes = tempInputStream.readAllBytes();
                tempInputStream.close();

                inputStream = new ByteArrayInputStream(fileBytes);
                fileName = buffer.getFileName();
                contentType = buffer.getFileData().getMimeType();
                fileSize = fileBytes.length;
            } else {
                // Handle case where no file is uploaded if necessary, 
                // currently service call will proceed with nulls/zero
            }
            
            materialService.saveLearningMaterial(material, inputStream, fileName, contentType, fileSize, user);
            
            Notification.show("Material saved successfully", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            clearForm();
            
        } catch (Exception e) {
            Notification.show("Error saving material: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private User findUserByUsername(String username) {
        // TODO: Replace with actual implementation that fetches User by username
        // This is a placeholder for now
        return new User();
    }

    private String determineContentType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".doc")) {
            return "application/msword";
        } else if (fileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }

    private void clearForm() {
        title.clear();
        title.setInvalid(false);
        
        description.clear();
        description.setInvalid(false);
        
        upload.clearFileList();
        
        material = new LearningMaterial();
    }

    public TextField getTitleField() {
        return title;
    }

    public TextArea getDescriptionField() {
        return description;
    }

    public Upload getFileUpload() {
        return upload;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public LearningMaterial getMaterial() {
        return material;
    }

    public User getUser() {
        return user;
    }
} 