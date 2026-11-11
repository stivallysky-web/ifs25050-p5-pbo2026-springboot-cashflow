package org.delcom.ifs25050springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
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
import org.delcom.ifs25050springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25050springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25050springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25050springbootcashflow.framework.component.FinancePresenter;
import org.delcom.ifs25050springbootcashflow.framework.component.ImageZoomDialog;
import org.delcom.ifs25050springbootcashflow.usecase.TransactionUseCase;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Catatan Keuangan Saya - Cashflow")
public class CashflowListView extends VerticalLayout implements BeforeEnterObserver {

    private final TransactionUseCase transactionUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<TransactionType> typeFilter = new ComboBox<>();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();

    private final Span totalIncomeSpan = new Span();
    private final Span totalExpenseSpan = new Span();
    private final Span netBalanceSpan = new Span();

    public CashflowListView(TransactionUseCase transactionUseCase, AuthSession authSession, FileStorage fileStorage) {
        this.transactionUseCase = transactionUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);

        H2 header = new H2("Ringkasan & Catatan Keuangan Saya");

        // 3 Kartu Ringkasan Saldo
        HorizontalLayout summaryCards = createSummaryCards();

        // Toolbar Filter dan Search
        searchField.setPlaceholder("Cari transaksi...");
        searchField.setWidth("250px");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshData());

        typeFilter.setPlaceholder("Semua Tipe");
        typeFilter.setItems(TransactionType.values());
        typeFilter.setItemLabelGenerator(TransactionType::getLabel);
        typeFilter.setClearButtonVisible(true);
        typeFilter.setWidth("180px");
        typeFilter.addValueChangeListener(e -> refreshData());

        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(SortOption::getLabel);
        sortBox.setValue(SortOption.NEWEST);
        sortBox.setWidth("200px");
        sortBox.addValueChangeListener(e -> refreshData());

        Button addBtn = new Button("Tambah Transaksi", e -> openTransactionDialog(null));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, typeFilter, sortBox, addBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);

        configureGrid();

        add(header, summaryCards, toolbar, grid);
    }

    private HorizontalLayout createSummaryCards() {
        VerticalLayout incomeCard = new VerticalLayout(new Span("Total Pemasukan"), totalIncomeSpan);
        incomeCard.getStyle().set("background-color", "#e6f4ea").set("border-radius", "8px").set("padding", "15px");
        totalIncomeSpan.getStyle().set("font-size", "1.4em").set("font-weight", "bold").set("color", "#137333");

        VerticalLayout expenseCard = new VerticalLayout(new Span("Total Pengeluaran"), totalExpenseSpan);
        expenseCard.getStyle().set("background-color", "#fce8e6").set("border-radius", "8px").set("padding", "15px");
        totalExpenseSpan.getStyle().set("font-size", "1.4em").set("font-weight", "bold").set("color", "#c5221f");

        VerticalLayout balanceCard = new VerticalLayout(new Span("Saldo Bersih"), netBalanceSpan);
        balanceCard.getStyle().set("background-color", "#e8f0fe").set("border-radius", "8px").set("padding", "15px");
        netBalanceSpan.getStyle().set("font-size", "1.4em").set("font-weight", "bold").set("color", "#1a73e8");

        HorizontalLayout cards = new HorizontalLayout(incomeCard, expenseCard, balanceCard);
        cards.setWidthFull();
        cards.setFlexGrow(1, incomeCard, expenseCard, balanceCard);
        return cards;
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addComponentColumn(t -> {
            if (t.getAttachmentPath() != null && !t.getAttachmentPath().isBlank()) {
                StreamResource resource = new StreamResource(t.getAttachmentPath(),
                        () -> new ByteArrayInputStream(fileStorage.loadAsBytes(t.getAttachmentPath())));
                Image img = new Image(resource, "Bukti");
                img.setWidth("45px");
                img.setHeight("45px");
                img.getStyle().set("object-fit", "cover").set("cursor", "pointer").set("border-radius", "4px");
                img.addClickListener(e -> new ImageZoomDialog(img.getSrc(), t.getTitle()).open());
                return img;
            }
            return new Image();
        }).setHeader("Bukti").setWidth("75px").setFlexGrow(0);

        grid.addColumn(Transaction::getTitle).setHeader("Judul Transaksi").setSortable(true);

        grid.addComponentColumn(t -> {
            Span typeSpan = new Span(t.getType().getLabel());
            if (t.getType() == TransactionType.INCOME) {
                typeSpan.getElement().getThemeList().add("badge success");
            } else {
                typeSpan.getElement().getThemeList().add("badge error");
            }
            return typeSpan;
        }).setHeader("Tipe").setWidth("120px");

        grid.addComponentColumn(t -> {
            Span amountSpan = new Span(FinancePresenter.formatRupiah(t.getAmount()));
            amountSpan.getStyle().set("font-weight", "bold");
            if (t.getType() == TransactionType.INCOME) {
                amountSpan.getStyle().set("color", "#137333");
            } else {
                amountSpan.getStyle().set("color", "#c5221f");
            }
            return amountSpan;
        }).setHeader("Nominal").setSortable(true);

        grid.addColumn(Transaction::getDescription).setHeader("Keterangan");

        grid.addComponentColumn(t -> {
            Button editBtn = new Button("Edit", e -> openTransactionDialog(t));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button deleteBtn = new Button("Hapus", e -> {
                transactionUseCase.delete(t.getId(), authSession.getCurrentUser().get().getId());
                refreshData();
                Notification.show("Transaksi berhasil dihapus", 2000, Notification.Position.BOTTOM_START);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Aksi");
    }

    private void refreshData() {
        if (!authSession.isLoggedIn()) return;
        Long userId = authSession.getCurrentUser().get().getId();

        // Update Saldo
        totalIncomeSpan.setText(FinancePresenter.formatRupiah(transactionUseCase.getTotalIncome(userId)));
        totalExpenseSpan.setText(FinancePresenter.formatRupiah(transactionUseCase.getTotalExpense(userId)));
        netBalanceSpan.setText(FinancePresenter.formatRupiah(transactionUseCase.getBalance(userId)));

        // Update Tabel
        List<Transaction> list = transactionUseCase.search(userId, searchField.getValue(), typeFilter.getValue(), sortBox.getValue());
        grid.setItems(list);
    }

    private void openTransactionDialog(Transaction transaction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(transaction == null ? "Catat Transaksi Baru" : "Edit Transaksi");

        TextField titleField = new TextField("Judul Transaksi");
        titleField.setWidthFull();
        titleField.setRequired(true);

        ComboBox<TransactionType> typeSelect = new ComboBox<>("Tipe Transaksi");
        typeSelect.setItems(TransactionType.values());
        typeSelect.setItemLabelGenerator(TransactionType::getLabel);
        typeSelect.setValue(TransactionType.EXPENSE);
        typeSelect.setWidthFull();

        NumberField amountField = new NumberField("Nominal (Rp)");
        amountField.setWidthFull();
        amountField.setMin(1);

        TextArea descArea = new TextArea("Keterangan");
        descArea.setWidthFull();

        AtomicReference<String> uploadedPath = new AtomicReference<>(transaction != null ? transaction.getAttachmentPath() : null);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.addSucceededListener(event -> {
            String savedFilename = fileStorage.store(buffer.getInputStream(), event.getFileName());
            uploadedPath.set(savedFilename);
            Notification.show("Bukti transaksi berhasil diunggah", 2000, Notification.Position.BOTTOM_START);
        });

        if (transaction != null) {
            titleField.setValue(transaction.getTitle());
            typeSelect.setValue(transaction.getType());
            amountField.setValue(transaction.getAmount());
            if (transaction.getDescription() != null) descArea.setValue(transaction.getDescription());
        }

        VerticalLayout form = new VerticalLayout(titleField, typeSelect, amountField, descArea, upload);
        dialog.add(form);

        Button saveBtn = new Button("Simpan", e -> {
            if (titleField.getValue().isBlank() || amountField.getValue() == null || amountField.getValue() <= 0) {
                Notification.show("Judul dan nominal transaksi harus diisi dengan benar!", 2000, Notification.Position.MIDDLE);
                return;
            }
            Long userId = authSession.getCurrentUser().get().getId();
            if (transaction == null) {
                transactionUseCase.create(userId, titleField.getValue(), descArea.getValue(),
                        typeSelect.getValue(), amountField.getValue(), uploadedPath.get());
            } else {
                transactionUseCase.update(transaction.getId(), userId, titleField.getValue(),
                        descArea.getValue(), typeSelect.getValue(), amountField.getValue(), uploadedPath.get());
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