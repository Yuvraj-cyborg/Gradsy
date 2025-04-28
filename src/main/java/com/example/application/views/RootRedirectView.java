package com.example.application.views;

import com.example.application.security.SecurityService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Route("")
@AnonymousAllowed // Allow access for redirection logic even if not logged in
public class RootRedirectView extends VerticalLayout implements BeforeEnterObserver {

    private final SecurityService securityService;

    @Autowired
    public RootRedirectView(SecurityService securityService) {
        this.securityService = securityService;
        // This view should be empty, its purpose is only redirection
        setSizeFull(); 
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        UserDetails userDetails = securityService.getAuthenticatedUser();

        if (userDetails != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // Check roles and redirect
                boolean isTeacher = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch("ROLE_TEACHER"::equals);
                boolean isStudent = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch("ROLE_STUDENT"::equals);

                if (isTeacher) {
                    event.forwardTo("teacher");
                } else if (isStudent) {
                    event.forwardTo("student");
                } else {
                    // Optional: Handle other roles or default if neither
                    // For now, forward to login if role is unrecognized
                    event.forwardTo("login"); 
                }
            } else {
                 // Should not happen if userDetails is not null, but handle defensively
                 event.forwardTo("login");
            }
        } else {
            // If not authenticated, forward to login view
            event.forwardTo("login");
        }
    }
} 