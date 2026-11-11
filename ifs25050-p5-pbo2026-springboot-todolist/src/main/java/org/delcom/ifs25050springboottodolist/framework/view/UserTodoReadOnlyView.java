package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import org.delcom.ifs25050springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25050springbootcashflow.domain.entity.User;
import org.delcom.ifs25050springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25050springbootcashflow.framework.component.ImageZoomDialog;
import org.delcom.ifs25050springbootcashflow.usecase.UserUseCase;

import java.io.ByteArrayInputStream;
import java.util.List;

@Route(value = "admin/users/:userId/todos", layout = MainLayout.class)
@PageTitle("Todo Pengguna - Read Only")
public class UserTodoReadOnlyView extends VerticalLayout implements BeforeEnterObserver, HasUrlParameter<String> {

    private final TodoUseCase todoUseCase;
    private final UserUseCase userUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;

    private Long targetUserId;
    private final H2 header = new H2("Daftar Todo Pengguna (Read-Only)");
    private final Grid<Todo> grid = new Grid<>(Todo.class, false);

    public UserTodoReadOnlyView(TodoUseCase todoUseCase, UserUseCase userUseCase,
                                AuthSession authSession, FileStorage fileStorage) {
        this.todoUseCase = todoUseCase;
        this.userUseCase = userUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);

        Button backBtn = new Button("Kembali ke Kelola Pengguna", e ->
                getUI().ifPresent(ui -> ui.navigate(UserManagementView.class)));

        grid.setSizeFull();

        grid.addComponentColumn(todo -> {
            if (todo.getCoverPath() != null && !todo.getCoverPath().isBlank()) {
                StreamResource resource = new StreamResource(todo.getCoverPath(),
                        () -> new ByteArrayInputStream(fileStorage.loadAsBytes(todo.getCoverPath())));
                Image img = new Image(resource, "Cover");
                img.setWidth("45px");
                img.setHeight("45px");
                img.getStyle().set("object-fit", "cover").set("cursor", "pointer").set("border-radius", "4px");
                img.addClickListener(e -> new ImageZoomDialog(img.getSrc(), todo.getTitle()).open());
                return img;
            }
            return new Image();
        }).setHeader("Cover").setWidth("80px").setFlexGrow(0);

        grid.addColumn(Todo::getTitle).setHeader("Judul");
        grid.addColumn(Todo::getDescription).setHeader("Deskripsi");
        grid.addColumn(t -> t.isCompleted() ? "Sudah Selesai" : "Belum Selesai").setHeader("Status");

        add(new HorizontalLayout(backBtn), header, grid);
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            this.targetUserId = Long.parseLong(parameter);
        } catch (NumberFormatException e) {
            this.targetUserId = null;
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authSession.isLoggedIn() || !authSession.isAdmin()) {
            event.forwardTo(TodoListView.class);
            return;
        }

        if (targetUserId == null) {
            event.forwardTo(UserManagementView.class);
            return;
        }

        try {
            User targetUser = userUseCase.findById(targetUserId);
            header.setText("Daftar Todo: " + targetUser.getName() + " (" + targetUser.getEmail() + ")");
            List<Todo> todos = todoUseCase.findByUserId(targetUserId);
            grid.setItems(todos);
        } catch (Exception e) {
            event.forwardTo(UserManagementView.class);
        }
    }
}