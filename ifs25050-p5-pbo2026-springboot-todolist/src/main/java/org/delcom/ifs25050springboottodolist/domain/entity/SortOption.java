package org.delcom.ifs25050springbootcashflow.domain.entity;

public enum SortOption {
    NEWEST("Terbaru"),
    OLDEST("Terlama"),
    TITLE_ASC("Judul A-Z"),
    TITLE_DESC("Judul Z-A"),
    STATUS_INCOMPLETE_FIRST("Belum Selesai"),
    STATUS_COMPLETED_FIRST("Sudah Selesai");

    private final String label;

    SortOption(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}