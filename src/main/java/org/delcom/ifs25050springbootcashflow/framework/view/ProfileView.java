package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
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
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil Saya")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;

    public ProfileView(UserUseCase userUseCase, AuthSession authSession, FileStorage fileStorage) {
        this.userUseCase = userUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        getElement().setAttribute("role", "main");

        setSizeFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
    }

    private void buildUI() {
        removeAll();
        User currentUser = authSession.getCurrentUser().orElse(null);
        if (currentUser == null) return;

        User user = userUseCase.findById(currentUser.getId());

        H2 header = new H2("Profil Pengguna");

        Avatar avatar = new Avatar(user.getName());
        avatar.setWidth("100px");
        avatar.setHeight("100px");
        if (user.getPhotoPath() != null && !user.getPhotoPath().isBlank()) {
            StreamResource resource = new StreamResource(user.getPhotoPath(),
                    () -> new ByteArrayInputStream(fileStorage.loadAsBytes(user.getPhotoPath())));
            avatar.setImageResource(resource);
        }

        TextField nameField = new TextField("Nama Lengkap");
        nameField.setValue(user.getName());
        nameField.setWidth("350px");

        EmailField emailField = new EmailField("Email");
        emailField.setValue(user.getEmail());
        emailField.setReadOnly(true);
        emailField.setWidth("350px");

        AtomicReference<String> newPhotoPath = new AtomicReference<>(user.getPhotoPath());

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.addSucceededListener(event -> {
            String savedFile = fileStorage.store(buffer.getInputStream(), event.getFileName());
            newPhotoPath.set(savedFile);
            Notification.show("Foto profil berhasil diunggah", 2000, Notification.Position.BOTTOM_START);
        });

        Button updateProfileBtn = new Button("Simpan Perubahan Profil", e -> {
            try {
                User updated = userUseCase.updateProfile(user.getId(), nameField.getValue(), newPhotoPath.get());
                authSession.login(updated);
                Notification.show("Profil berhasil diperbarui", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                buildUI();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        updateProfileBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateProfileBtn.setWidth("350px");

        H3 passHeader = new H3("Ubah Password");
        PasswordField oldPass = new PasswordField("Password Lama");
        oldPass.setWidth("350px");
        PasswordField newPass = new PasswordField("Password Baru (min 6 karakter)");
        newPass.setWidth("350px");

        Button changePassBtn = new Button("Ubah Password", e -> {
            try {
                userUseCase.changePassword(user.getId(), oldPass.getValue(), newPass.getValue());
                Notification.show("Password berhasil diubah", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                oldPass.clear();
                newPass.clear();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        changePassBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changePassBtn.setWidth("350px");

        VerticalLayout profileForm = new VerticalLayout(header, avatar, upload, nameField, emailField, updateProfileBtn,
                passHeader, oldPass, newPass, changePassBtn);
        profileForm.setWidth("400px");
        profileForm.setAlignItems(FlexComponent.Alignment.CENTER);

        add(profileForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authSession.isLoggedIn()) {
            event.forwardTo(LoginView.class);
        } else {
            buildUI();
        }
    }
}