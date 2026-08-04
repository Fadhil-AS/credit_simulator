package id.co.bcadigital.credit.core.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);

    private MoneyFormatter() {
    }

    public static String rupiah(BigDecimal amount) {
        return "Rp. " + new DecimalFormat("#,##0.00", SYMBOLS)
                .format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    public static String percent(BigDecimal rate) {
        BigDecimal percentage = rate.movePointRight(2).stripTrailingZeros();
        return new DecimalFormat("0.###", SYMBOLS).format(percentage).replace('.', ',') + "%";
    }
}
