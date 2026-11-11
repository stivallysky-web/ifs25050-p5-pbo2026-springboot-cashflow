package org.delcom.ifs25050springbootcashflow.framework.component;

import java.text.NumberFormat;
import java.util.Locale;

public class FinancePresenter {

    private static final Locale INDONESIA = Locale.of("id", "ID");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(INDONESIA);

    public static String formatRupiah(Double amount) {
        if (amount == null) return "Rp 0";
        return CURRENCY_FORMAT.format(amount).replace(",00", "");
    }
}