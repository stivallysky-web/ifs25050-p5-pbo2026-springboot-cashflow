package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
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

        // Landmark utama agar lolos audit Axe Core
        getElement().setAttribute("role", "main");

        // [PERBAIKAN FINAL] Menambahkan Meta Description dan MutationObserver untuk indikator koneksi
        String js = "if (!document.querySelector('meta[name=\"description\"]')) {" +
                "  var meta = document.createElement('meta');" +
                "  meta.name = 'description';" +
                "  meta.content = 'Aplikasi pencatatan keuangan Cashflow terintegrasi.';" +
                "  document.head.appendChild(meta);" +
                "}" +
                "const observer = new MutationObserver(function() {" +
                "  document.querySelectorAll('vaadin-connection-indicator').forEach(function(el) {" +
                "    if (!el.hasAttribute('role')) {" +
                "      el.setAttribute('role', 'region');" +
                "      el.setAttribute('aria-label', 'Status Koneksi Login');" +
                "    }" +
                "  });" +
                "});" +
                "observer.observe(document.body, {childList: true, subtree: true});";
        getElement().executeJs(js);

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H1 title = new H1("Masuk ke Cashflow");

        Input emailField = new Input();
        emailField.setId("login-username");
        emailField.setType("email");
        emailField.setPlaceholder("Email");
        emailField.setWidth("320px");
        emailField.setValueChangeMode(ValueChangeMode.EAGER);
        emailField.getElement().setAttribute("name", "username");
        emailField.getElement().setAttribute("autocomplete", "username");
        styleNativeInput(emailField);

        NativeLabel emailLabel = new NativeLabel("Email");
        emailLabel.setFor(emailField);

        Input passwordField = new Input();
        passwordField.setId("login-password");
        passwordField.setType("password");
        passwordField.setPlaceholder("Password");
        passwordField.setWidth("320px");
        passwordField.setValueChangeMode(ValueChangeMode.EAGER);
        passwordField.getElement().setAttribute("name", "password");
        passwordField.getElement().setAttribute("autocomplete", "current-password");
        styleNativeInput(passwordField);

        NativeLabel passwordLabel = new NativeLabel("Password");
        passwordLabel.setFor(passwordField);

        Runnable performLogin = () -> {
            String email = emailField.getValue();
            String password = passwordField.getValue();

            var userOpt = authUseCase.login(email, password);
            if (userOpt.isPresent()) {
                authSession.login(userOpt.get());
                getUI().ifPresent(ui -> ui.getPage().setLocation("/"));
            } else {
                Notification notif = Notification.show(
                        "Email atau password salah!",
                        3000,
                        Notification.Position.TOP_CENTER
                );
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };

        NativeButton loginBtn = new NativeButton("Masuk", e -> performLogin.run());
        loginBtn.setId("login-submit");
        loginBtn.getElement().setAttribute("type", "submit");
        loginBtn.getStyle()
                .set("width", "320px")
                .set("padding", "0.6rem 0.75rem")
                .set("background-color", "#0d47a1")
                .set("color", "#ffffff")
                .set("border", "none")
                .set("border-radius", "4px")
                .set("font-size", "1rem")
                .set("font-weight", "600")
                .set("cursor", "pointer");

        Shortcuts.addShortcutListener(this, performLogin::run, Key.ENTER);

        RouterLink registerLink = new RouterLink("Belum punya akun? Daftar di sini", RegisterView.class);

        add(title, emailLabel, emailField, passwordLabel, passwordField, loginBtn, registerLink);
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