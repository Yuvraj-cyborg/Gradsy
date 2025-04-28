package com.example.application.views;

import com.example.application.data.UserRole;
import com.example.application.services.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route("signup")
@PageTitle("Sign Up | Gradsy")
@AnonymousAllowed
public class SignupView extends VerticalLayout {

    private final UserService userService;

    private TextField usernameField;
    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField firstNameField;
    private TextField lastNameField;
    private RadioButtonGroup<UserRole> roleRadioGroup;
    private TextField subjectAreaField;
    private Button signupButton;

    // Using a simple DTO for binding
    public static class SignupFormDTO {
        private String username;
        private String email;
        private String password;
        private String confirmPassword;
        private String firstName;
        private String lastName;
        private UserRole role;
        private String subjectArea;

        // Getters and Setters needed for Binder
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
        public String getSubjectArea() { return subjectArea; }
        public void setSubjectArea(String subjectArea) { this.subjectArea = subjectArea; }
    }

    private BeanValidationBinder<SignupFormDTO> binder;

    @Autowired
    public SignupView(UserService userService) {
        this.userService = userService;

        addClassName("signup-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        createForm();
        setupBinder();

        add(new H2("Create Account"), createFormLayout());
    }

    private void createForm() {
        usernameField = new TextField("Username");
        emailField = new EmailField("Email");
        passwordField = new PasswordField("Password");
        confirmPasswordField = new PasswordField("Confirm Password");
        firstNameField = new TextField("First Name");
        lastNameField = new TextField("Last Name");
        roleRadioGroup = new RadioButtonGroup<>("I am a:");
        roleRadioGroup.setItems(UserRole.STUDENT, UserRole.TEACHER);
        roleRadioGroup.setValue(UserRole.STUDENT); // Default selection
        subjectAreaField = new TextField("Subject Taught");
        subjectAreaField.setVisible(false); // Initially hidden

        signupButton = new Button("Sign Up");
        signupButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Show/hide subject field based on role
        roleRadioGroup.addValueChangeListener(event -> {
            subjectAreaField.setVisible(event.getValue() == UserRole.TEACHER);
        });

        signupButton.addClickListener(e -> register());
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(usernameField, emailField,
                       passwordField, confirmPasswordField,
                       firstNameField, lastNameField,
                       roleRadioGroup, subjectAreaField,
                       signupButton);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(roleRadioGroup, 2);
        formLayout.setColspan(subjectAreaField, 2);
        formLayout.setColspan(signupButton, 2);
        return formLayout;
    }

    private void setupBinder() {
        binder = new BeanValidationBinder<>(SignupFormDTO.class);

        binder.forField(usernameField).asRequired("Username is required").bind("username");
        binder.forField(emailField).asRequired("Email is required")
              .withValidator(new EmailValidator("Invalid email address"))
              .bind("email");
        binder.forField(passwordField).asRequired("Password is required").bind("password");
        binder.forField(confirmPasswordField).asRequired("Confirm password")
              .withValidator((value, context) -> {
                  if (value != null && value.equals(passwordField.getValue())) {
                      return com.vaadin.flow.data.binder.ValidationResult.ok();
                  } else {
                      return com.vaadin.flow.data.binder.ValidationResult.error("Passwords do not match");
                  }
              })
              .bind("confirmPassword"); // Not strictly needed to bind confirmPassword if not in DTO logic
        binder.forField(firstNameField).asRequired("First name is required").bind("firstName");
        binder.forField(lastNameField).asRequired("Last name is required").bind("lastName");
        binder.forField(roleRadioGroup).asRequired("Role selection is required").bind("role");
        // Subject area binding, required only if role is Teacher
        binder.forField(subjectAreaField)
              .withValidator((value, context) -> {
                  if (roleRadioGroup.getValue() == UserRole.TEACHER && (value == null || value.trim().isEmpty())) {
                      return com.vaadin.flow.data.binder.ValidationResult.error("Subject is required for teachers");
                  }
                  return com.vaadin.flow.data.binder.ValidationResult.ok();
              })
              .bind("subjectArea");
              
        binder.setBean(new SignupFormDTO()); // Initialize with an empty DTO
    }

    private void register() {
        try {
            SignupFormDTO signupData = new SignupFormDTO();
            binder.writeBean(signupData); // Validate and write form data to DTO

            userService.registerUser(
                signupData.getUsername(),
                signupData.getPassword(),
                signupData.getEmail(),
                signupData.getRole(),
                signupData.getFirstName(),
                signupData.getLastName(),
                signupData.getRole() == UserRole.TEACHER ? signupData.getSubjectArea() : null
            );

            Notification.show("Registration successful! Please log in.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            // Redirect to login page
            UI.getCurrent().navigate(LoginView.class);

        } catch (ValidationException e) {
            // Fields will show validation errors
            Notification.show("Please check the form for errors.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        } catch (Exception e) {
            // Handle registration errors (e.g., username exists)
            Notification.show("Registration failed: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
} 