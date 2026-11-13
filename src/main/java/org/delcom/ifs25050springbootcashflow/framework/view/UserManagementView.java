package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.delcom.ifs25050springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25050springbootcashflow.domain.entity.User;
import org.delcom.ifs25050springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25050springbootcashflow.usecase.UserUseCase;

import java.io.ByteArrayInputStream;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Kelola Pengguna - Admin")
public class UserManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UserManagementView(UserUseCase userUseCase, AuthSession authSession, FileStorage fileStorage) {
        this.userUseCase = userUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        getElement().setAttribute("role", "main");
        
        setSizeFull();
        setPadding(true);

        H2 header = new H2("Daftar Seluruh Pengguna (Admin)");

        grid.setSizeFull();

        grid.addComponentColumn(user -> {
            Avatar avatar = new Avatar(user.getName());
            if (user.getPhotoPath() != null && !user.getPhotoPath().isBlank()) {
                StreamResource resource = new StreamResource(user.getPhotoPath(),
                        () -> new ByteArrayInputStream(fileStorage.loadAsBytes(user.getPhotoPath())));
                avatar.setImageResource(resource);
            }
            return avatar;
        }).setHeader("Foto").setWidth("70px").setFlexGrow(0);

        grid.addColumn(User::getId).setHeader("ID").setWidth("60px").setFlexGrow(0);
        grid.addColumn(User::getName).setHeader("Nama").setSortable(true);
        grid.addColumn(User::getEmail).setHeader("Email").setSortable(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Role");

        grid.addComponentColumn(user -> {
            Button viewTransactionsBtn = new Button("Lihat Keuangan", e -> {
                UI.getCurrent().navigate(UserTransactionReadOnlyView.class, user.getId().toString());
            });
            viewTransactionsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            return viewTransactionsBtn;
        }).setHeader("Aksi");

        add(header, grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authSession.isLoggedIn() || !authSession.isAdmin()) {
            event.forwardTo(CashflowListView.class);
        } else {
            grid.setItems(userUseCase.findAll());
        }
    }
}