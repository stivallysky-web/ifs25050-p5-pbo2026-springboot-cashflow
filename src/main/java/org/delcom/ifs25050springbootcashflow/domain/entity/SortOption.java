package org.delcom.ifs25050springbootcashflow.domain.entity;

public enum SortOption {
    NEWEST("Terbaru"),
    OLDEST("Terlama"),
    TITLE_ASC("Judul A-Z"),
    TITLE_DESC("Judul Z-A"),
    AMOUNT_HIGH("Nominal Terbesar"),
    AMOUNT_LOW("Nominal Terkecil");

    private final String label;

    SortOption(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}