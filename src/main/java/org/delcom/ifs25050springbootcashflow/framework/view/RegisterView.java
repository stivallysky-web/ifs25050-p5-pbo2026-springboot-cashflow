package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
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
@PageTitle("Daftar Akun - Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final AuthSession authSession;

    public RegisterView(AuthUseCase authUseCase, AuthSession authSession) {
        this.authUseCase = authUseCase;
        this.authSession = authSession;

        // [PERUBAHAN 1] Menambahkan role="main" untuk mencegah Axe violation (landmark-one-main & region)
        getElement().setAttribute("role", "main");

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // [PERUBAHAN 2] Mengubah H2 menjadi H1 untuk mencegah Axe violation (page-has-heading-one)
        H1 title = new H1("Pendaftaran Akun Baru");

        // [PERUBAHAN 3] Menggunakan Input & NativeLabel dengan pasangan id-for yang eksplisit
        Input nameField = new Input();
        nameField.setId("register-name");
        nameField.setType("text");
        nameField.setPlaceholder("Nama Lengkap");
        nameField.setWidth("320px");
        nameField.setValueChangeMode(ValueChangeMode.EAGER);
        styleNativeInput(nameField);

        NativeLabel nameLabel = new NativeLabel("Nama Lengkap");
        nameLabel.setFor(nameField);

        Input emailField = new Input();
        emailField.setId("register-email");
        emailField.setType("email");
        emailField.setPlaceholder("Email");
        emailField.setWidth("320px");
        emailField.setValueChangeMode(ValueChangeMode.EAGER);
        styleNativeInput(emailField);

        NativeLabel emailLabel = new NativeLabel("Email");
        emailLabel.setFor(emailField);

        Input passwordField = new Input();
        passwordField.setId("register-password");
        passwordField.setType("password");
        passwordField.setPlaceholder("Password (min 6 karakter)");
        passwordField.setWidth("320px");
        passwordField.setValueChangeMode(ValueChangeMode.EAGER);
        styleNativeInput(passwordField);

        NativeLabel passwordLabel = new NativeLabel("Password (min 6 karakter)");
        passwordLabel.setFor(passwordField);

        NativeButton registerBtn = new NativeButton("Daftar Sekarang", e -> {
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
        registerBtn.setId("register-submit");
        registerBtn.getElement().setAttribute("type", "submit");
        registerBtn.getStyle()
                .set("width", "320px")
                .set("padding", "0.6rem 0.75rem")
                .set("background-color", "#0d47a1")
                .set("color", "#ffffff")
                .set("border", "none")
                .set("border-radius", "4px")
                .set("font-size", "1rem")
                .set("font-weight", "600")
                .set("cursor", "pointer");

        RouterLink loginLink = new RouterLink("Sudah punya akun? Masuk di sini", LoginView.class);

        add(title, nameLabel, nameField, emailLabel, emailField, passwordLabel, passwordField, registerBtn, loginLink);
    }

    private static void styleNativeInput(Input input) {
        input.getStyle()
                .set("padding", "0.5rem 0.75rem")
                .set("border", "1px solid var(--lumo-contrast-30pct, #c5c5c5)")
                .set("border-radius", "var(--lumo-border-radius-m, 4px)")
                .set("font-size", "1rem")
                .set("font-family", "inherit")
                .set("box-sizing", "border-box");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.isLoggedIn()) {
            event.forwardTo(CashflowListView.class);
        }
    }
}