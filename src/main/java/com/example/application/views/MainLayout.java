package com.example.application.views;

import com.example.application.data.User;
import com.example.application.security.SecurityService;
import com.example.application.services.UserService;
import com.example.application.views.student.StudentDashboardView;
import com.example.application.views.teacher.TeacherDashboardView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.userdetails.UserDetails;

public class MainLayout extends AppLayout {
    private final SecurityService securityService;
    private final UserService userService;

    public MainLayout(SecurityService securityService, UserService userService) {
        this.securityService = securityService;
        this.userService = userService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Gradst");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM);

        String username = securityService.getAuthenticatedUser().getUsername();
        Button logout = new Button("Log out " + username, e -> securityService.logout());

        var header = new HorizontalLayout(new DrawerToggle(), logo, logout);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(
            LumoUtility.Padding.Vertical.NONE,
            LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header); 
    }

    private void createDrawer() {
        VerticalLayout layout = new VerticalLayout();
        
        // Common links
        // layout.add(new RouterLink("Home", DashboardView.class)); // Removed link to non-existent DashboardView
        
        // Get current user
        UserDetails userDetails = securityService.getAuthenticatedUser();
        if (userDetails != null) {
            String username = userDetails.getUsername();
            User user = userService.findUserByUsername(username);
            
            // Add role-specific navigation links
            if (user != null) {
                if (userService.isStudent(user)) {
                    layout.add(new RouterLink("Student Dashboard", StudentDashboardView.class));
                }
                
                if (userService.isTeacher(user)) {
                    layout.add(new RouterLink("Teacher Dashboard", TeacherDashboardView.class));
                }
            }
        }
        
        addToDrawer(layout);
    }
}