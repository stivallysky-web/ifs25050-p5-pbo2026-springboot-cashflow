package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import org.delcom.ifs25050springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25050springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25050springbootcashflow.domain.entity.User;
import org.delcom.ifs25050springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25050springbootcashflow.framework.component.FinancePresenter;
import org.delcom.ifs25050springbootcashflow.framework.component.ImageZoomDialog;
import org.delcom.ifs25050springbootcashflow.usecase.TransactionUseCase;
import org.delcom.ifs25050springbootcashflow.usecase.UserUseCase;

import java.io.ByteArrayInputStream;
import java.util.List;

@Route(value = "admin/users/:userId/transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna - Read Only")
public class UserTransactionReadOnlyView extends VerticalLayout implements BeforeEnterObserver, HasUrlParameter<String> {

    private final TransactionUseCase transactionUseCase;
    private final UserUseCase userUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;

    private Long targetUserId;
    private final H2 header = new H2("Daftar Transaksi Pengguna (Read-Only)");
    private final Span balanceInfo = new Span();
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);

    public UserTransactionReadOnlyView(TransactionUseCase transactionUseCase, UserUseCase userUseCase,
                                       AuthSession authSession, FileStorage fileStorage) {
        this.transactionUseCase = transactionUseCase;
        this.userUseCase = userUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);

        Button backBtn = new Button("Kembali ke Kelola Pengguna", e ->
                getUI().ifPresent(ui -> ui.navigate(UserManagementView.class)));

        // [PERUBAHAN 1] Menggunakan warna biru tua #0d47a1 untuk kontras maksimal
        balanceInfo.getStyle().set("font-weight", "bold").set("color", "#0d47a1").set("font-size", "1.2em");

        grid.setSizeFull();

        grid.addComponentColumn(t -> {
            if (t.getAttachmentPath() != null && !t.getAttachmentPath().isBlank()) {
                StreamResource resource = new StreamResource(t.getAttachmentPath(),
                        () -> new ByteArrayInputStream(fileStorage.loadAsBytes(t.getAttachmentPath())));
                Image img = new Image(resource, "Bukti " + t.getTitle());
                img.setWidth("45px");
                img.setHeight("45px");
                img.getStyle().set("object-fit", "cover").set("cursor", "pointer").set("border-radius", "4px");
                img.addClickListener(e -> new ImageZoomDialog(img.getSrc(), t.getTitle()).open());
                return img;
            }
            // [PERUBAHAN 2] Mengganti Image() kosong menjadi Span("-") agar tidak melanggar aturan image-alt
            return new Span("-");
        }).setHeader("Bukti").setWidth("75px").setFlexGrow(0);

        grid.addColumn(Transaction::getTitle).setHeader("Judul Transaksi");
        grid.addColumn(t -> t.getType().getLabel()).setHeader("Tipe");
        grid.addColumn(t -> FinancePresenter.formatRupiah(t.getAmount())).setHeader("Nominal");
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan");

        add(new HorizontalLayout(backBtn), header, balanceInfo, grid);
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
            event.forwardTo(CashflowListView.class);
            return;
        }

        if (targetUserId == null) {
            event.forwardTo(UserManagementView.class);
            return;
        }

        try {
            User targetUser = userUseCase.findById(targetUserId);
            Double balance = transactionUseCase.getBalance(targetUserId);
            header.setText("Transaksi: " + targetUser.getName() + " (" + targetUser.getEmail() + ")");
            balanceInfo.setText("Total Saldo Pengguna Saat Ini: " + FinancePresenter.formatRupiah(balance));

            List<Transaction> list = transactionUseCase.findByUserId(targetUserId);
            grid.setItems(list);
        } catch (Exception e) {
            event.forwardTo(UserManagementView.class);
        }
    }
}