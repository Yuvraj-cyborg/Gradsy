package com.example.application.views.teacher;

import com.example.application.data.LearningMaterial;
import com.example.application.data.User;
import com.example.application.security.SecurityService;
import com.example.application.services.LearningMaterialService;
import com.example.application.services.UserService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Optional;

@Route(value = "teacher/material", layout = MainLayout.class)
@PageTitle("Learning Material | Gradst")
@PermitAll
public class LearningMaterialFormView extends VerticalLayout implements HasUrlParameter<String> {
    
    private final LearningMaterialService materialService;
    private final UserService userService;
    private final SecurityService securityService;
    
    private LearningMaterial material;
    private User currentUser;
    
    private TextField titleField;
    private TextArea descriptionField;
    private Upload fileUpload;
    private MemoryBuffer memoryBuffer;
    
    private boolean isNewMaterial = true;
    
    @Autowired
    public LearningMaterialFormView(LearningMaterialService materialService,
                                   UserService userService,
                                   SecurityService securityService) {
        this.materialService = materialService;
        this.userService = userService;
        this.securityService = securityService;
        
        addClassName("material-form-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        
        String username = securityService.getAuthenticatedUser().getUsername();
        currentUser = userService.findUserByUsername(username);
        
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setMaxWidth("800px");
        formContainer.setWidth("100%");
        
        H2 title = new H2("Learning Material");
        formContainer.add(title);
        
        FormLayout formLayout = new FormLayout();
        
        titleField = new TextField("Title");
        titleField.setRequired(true);
        titleField.setWidth("100%");
        
        descriptionField = new TextArea("Description");
        descriptionField.setWidth("100%");
        
        memoryBuffer = new MemoryBuffer();
        fileUpload = new Upload(memoryBuffer);
        fileUpload.setMaxFiles(1);
        fileUpload.setAcceptedFileTypes("application/pdf", ".pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx", ".doc");
        fileUpload.setWidth("100%");
        
        formLayout.add(titleField, descriptionField, fileUpload);
        formLayout.setColspan(descriptionField, 2);
        formLayout.setColspan(fileUpload, 2);
        
        Button saveButton = new Button("Save", e -> saveMaterial());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("Cancel", e -> getUI().ifPresent(ui -> ui.navigate(TeacherDashboardView.class)));
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setWidth("100%");
        buttonLayout.setPadding(true);
        
        formContainer.add(formLayout, buttonLayout);
        add(formContainer);
    }
    
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        if (parameter.equals("new")) {
            material = new LearningMaterial();
            isNewMaterial = true;
        } else {
            try {
                Long materialId = Long.valueOf(parameter);
                Optional<LearningMaterial> materialOpt = materialService.findById(materialId);

                if (materialOpt.isPresent()) {
                    material = materialOpt.get();
                    isNewMaterial = false;
                    fillForm();
                } else {
                    material = new LearningMaterial();
                    Notification.show("Material with ID " + materialId + " not found.").addThemeVariants(NotificationVariant.LUMO_WARNING);
                }
            } catch (NumberFormatException e) {
                Notification.show("Invalid material ID").addThemeVariants(NotificationVariant.LUMO_ERROR);
                getUI().ifPresent(ui -> ui.navigate(TeacherDashboardView.class));
            }
        }
    }
    
    private void fillForm() {
        titleField.setValue(material.getTitle());
        if (material.getDescription() != null) {
            descriptionField.setValue(material.getDescription());
        }
        
        // Since we can't set the file in the upload component, we'll just indicate if there's a file
        if (material.getFilePath() != null) {
            fileUpload.setDropLabelIcon(null);
            fileUpload.setDropLabel(new Span("A file is already uploaded. Upload a new file to replace it."));
        }
    }
    
    private void saveMaterial() {
        if (titleField.isEmpty()) {
            Notification.show("Please enter a title").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        material.setTitle(titleField.getValue());
        material.setDescription(descriptionField.getValue());
        
        InputStream inputStream = null;
        String originalFilename = null;
        String contentType = null;
        long fileSize = 0;

        if (memoryBuffer.getInputStream() != null && memoryBuffer.getFileName() != null && !memoryBuffer.getFileName().isEmpty()) {
            try {
                InputStream tempInputStream = memoryBuffer.getInputStream();
                byte[] fileBytes = tempInputStream.readAllBytes();
                tempInputStream.close();

                inputStream = new ByteArrayInputStream(fileBytes);
                originalFilename = memoryBuffer.getFileName();
                contentType = memoryBuffer.getFileData().getMimeType();
                fileSize = fileBytes.length;
                
            } catch (IOException e) {
                Notification.show("Error reading file data: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            } catch (Exception e) {
                Notification.show("Error processing file: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }
        
        try {
            materialService.saveLearningMaterial(material, inputStream, originalFilename, contentType, fileSize, currentUser);
            Notification.show("Material saved successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate(TeacherDashboardView.class));
        } catch (Exception e) {
            Notification.show("Error saving material: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
} 