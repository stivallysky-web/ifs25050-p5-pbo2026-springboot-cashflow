package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.delcom.ifs25050springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25050springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25050springbootcashflow.domain.entity.User;
import org.delcom.ifs25050springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25050springbootcashflow.framework.component.ImageZoomDialog;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Daftar Todo Saya")
public class TodoListView extends VerticalLayout implements BeforeEnterObserver {

    private final TodoUseCase todoUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;

    private final Grid<Todo> grid = new Grid<>(Todo.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();

    public TodoListView(TodoUseCase todoUseCase, AuthSession authSession, FileStorage fileStorage) {
        this.todoUseCase = todoUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);

        H2 header = new H2("Daftar Todo Saya");

        searchField.setPlaceholder("Cari berdasarkan judul atau deskripsi...");
        searchField.setWidth("300px");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshData());

        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(SortOption::getLabel);
        sortBox.setValue(SortOption.NEWEST);
        sortBox.setWidth("200px");
        sortBox.addValueChangeListener(e -> refreshData());

        Button addBtn = new Button("Tambah Todo", e -> openTodoDialog(null));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, sortBox, addBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);

        configureGrid();

        add(header, toolbar, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addComponentColumn(todo -> {
            Checkbox checkbox = new Checkbox(todo.isCompleted());
            checkbox.addValueChangeListener(e -> {
                todoUseCase.toggleCompleted(todo.getId(), authSession.getCurrentUser().get().getId());
                refreshData();
            });
            return checkbox;
        }).setHeader("Selesai").setWidth("80px").setFlexGrow(0);

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

        grid.addColumn(Todo::getTitle).setHeader("Judul").setSortable(true);
        grid.addColumn(Todo::getDescription).setHeader("Deskripsi");

        grid.addComponentColumn(todo -> {
            Button editBtn = new Button("Edit", e -> openTodoDialog(todo));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button deleteBtn = new Button("Hapus", e -> {
                todoUseCase.delete(todo.getId(), authSession.getCurrentUser().get().getId());
                refreshData();
                Notification.show("Todo berhasil dihapus", 2000, Notification.Position.BOTTOM_START);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Aksi");
    }

    private void refreshData() {
        if (!authSession.isLoggedIn()) return;
        Long userId = authSession.getCurrentUser().get().getId();
        List<Todo> todos = todoUseCase.search(userId, searchField.getValue(), sortBox.getValue());
        grid.setItems(todos);
    }

    private void openTodoDialog(Todo todo) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(todo == null ? "Tambah Todo Baru" : "Edit Todo");

        TextField titleField = new TextField("Judul");
        titleField.setWidthFull();
        titleField.setRequired(true);

        TextArea descArea = new TextArea("Deskripsi");
        descArea.setWidthFull();

        AtomicReference<String> uploadedPath = new AtomicReference<>(todo != null ? todo.getCoverPath() : null);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.addSucceededListener(event -> {
            String savedFilename = fileStorage.store(buffer.getInputStream(), event.getFileName());
            uploadedPath.set(savedFilename);
            Notification.show("Foto cover berhasil diunggah", 2000, Notification.Position.BOTTOM_START);
        });

        if (todo != null) {
            titleField.setValue(todo.getTitle());
            if (todo.getDescription() != null) descArea.setValue(todo.getDescription());
        }

        VerticalLayout form = new VerticalLayout(titleField, descArea, upload);
        dialog.add(form);

        Button saveBtn = new Button("Simpan", e -> {
            if (titleField.getValue().isBlank()) {
                Notification.show("Judul wajib diisi", 2000, Notification.Position.MIDDLE);
                return;
            }
            Long userId = authSession.getCurrentUser().get().getId();
            if (todo == null) {
                todoUseCase.create(userId, titleField.getValue(), descArea.getValue(), uploadedPath.get());
            } else {
                todoUseCase.update(todo.getId(), userId, titleField.getValue(), descArea.getValue(), uploadedPath.get());
            }
            refreshData();
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Batal", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authSession.isLoggedIn()) {
            event.forwardTo(LoginView.class);
        } else {
            refreshData();
        }
    }
}