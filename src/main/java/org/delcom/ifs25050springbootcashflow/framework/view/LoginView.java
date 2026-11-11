package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25050springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25050springbootcashflow.usecase.AuthUseCase;

@Route("login")
@PageTitle("Login - Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final AuthSession authSession;

    public LoginView(AuthUseCase authUseCase, AuthSession authSession) {
        this.authUseCase = authUseCase;
        this.authSession = authSession;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H2 title = new H2("Masuk ke Cashflow");

        EmailField emailField = new EmailField("Email");
        emailField.setId("login-username");
        emailField.setWidth("320px");
        emailField.setRequiredIndicatorVisible(true);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setId("login-password");
        passwordField.setWidth("320px");
        passwordField.setRequiredIndicatorVisible(true);

        Button loginBtn = new Button("Masuk", e -> {
            String email = emailField.getValue();
            String password = passwordField.getValue();

            var userOpt = authUseCase.login(email, password);
            if (userOpt.isPresent()) {
                authSession.login(userOpt.get());
                getUI().ifPresent(ui -> ui.navigate(CashflowListView.class));
            } else {
                Notification notif = Notification.show("Email atau password salah!", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        loginBtn.setId("login-submit");
        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginBtn.setWidth("320px");

        RouterLink registerLink = new RouterLink("Belum punya akun? Daftar di sini", RegisterView.class);

        add(title, emailField, passwordField, loginBtn, registerLink);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.isLoggedIn()) {
            event.forwardTo(CashflowListView.class);
        }
    }
}