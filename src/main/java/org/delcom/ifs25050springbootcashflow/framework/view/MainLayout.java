package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25050springbootcashflow.framework.auth.AuthSession;

public class MainLayout extends AppLayout {

    private final AuthSession authSession;

    public MainLayout(AuthSession authSession) {
        this.authSession = authSession;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Cashflow App (Catatan Keuangan)");
        logo.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        Button logoutBtn = new Button("Keluar", e -> {
            authSession.logout();
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        Span userSpan = new Span();
        authSession.getCurrentUser().ifPresent(u -> userSpan.setText(u.getName() + " (" + u.getRole() + ")"));

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu Navigasi");

        HorizontalLayout header = new HorizontalLayout(toggle, logo, userSpan, logoutBtn);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        // Menandai header sebagai banner landmark agar lolos audit Axe Core
        header.getElement().setAttribute("role", "banner");

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout nav = new VerticalLayout();
        nav.add(new RouterLink("Daftar Transaksi Keuangan", CashflowListView.class));
        nav.add(new RouterLink("Profil Saya", ProfileView.class));

        if (authSession.isAdmin()) {
            nav.add(new RouterLink("Kelola Pengguna (Admin)", UserManagementView.class));
        }

        // Menandai drawer sebagai navigation landmark
        nav.getElement().setAttribute("role", "navigation");
        nav.getElement().setAttribute("aria-label", "Menu Utama Aplikasi");

        addToDrawer(nav);
    }
}