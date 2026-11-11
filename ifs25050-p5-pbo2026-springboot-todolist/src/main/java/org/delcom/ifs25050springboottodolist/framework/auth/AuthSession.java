package org.delcom.ifs25050springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinSession;
import org.delcom.ifs25050springbootcashflow.domain.entity.Role;
import org.delcom.ifs25050springbootcashflow.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthSession {

    private static final String USER_SESSION_KEY = "CURRENT_USER";

    public void login(User user) {
        VaadinSession.getCurrent().setAttribute(USER_SESSION_KEY, user);
    }

    public void logout() {
        VaadinSession.getCurrent().setAttribute(USER_SESSION_KEY, null);
        VaadinSession.getCurrent().close();
    }

    public Optional<User> getCurrentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((User) session.getAttribute(USER_SESSION_KEY));
    }

    public boolean isLoggedIn() {
        return getCurrentUser().isPresent();
    }

    public boolean isAdmin() {
        return getCurrentUser().map(User::isAdmin).orElse(false);
    }
}