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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25050springbootcashflow.domain.entity.Role;
import org.delcom.ifs25050springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25050springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25050springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Daftar Akun - Todo List")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final AuthSession authSession;

    public RegisterView(AuthUseCase authUseCase, AuthSession authSession) {
        this.authUseCase = authUseCase;
        this.authSession = authSession;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H2 title = new H2("Pendaftaran Akun Baru");

        TextField nameField = new TextField("Nama Lengkap");
        nameField.setWidth("320px");
        nameField.setRequiredIndicatorVisible(true);

        EmailField emailField = new EmailField("Email");
        emailField.setWidth("320px");
        emailField.setRequiredIndicatorVisible(true);

        PasswordField passwordField = new PasswordField("Password (min 6 karakter)");
        passwordField.setWidth("320px");
        passwordField.setRequiredIndicatorVisible(true);

        Button registerBtn = new Button("Daftar Sekarang", e -> {
            try {
                authUseCase.register(nameField.getValue(), emailField.getValue(), passwordField.getValue(), Role.USER);
                Notification notif = Notification.show("Registrasi berhasil! Silakan login.", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            } catch (ValidationException ex) {
                Notification notif = Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidth("320px");

        RouterLink loginLink = new RouterLink("Sudah punya akun? Masuk di sini", LoginView.class);

        add(title, nameField, emailField, passwordField, registerBtn, loginLink);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.isLoggedIn()) {
            event.forwardTo(TodoListView.class);
        }
    }
}