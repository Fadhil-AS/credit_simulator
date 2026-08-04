package id.co.bcadigital.credit.core.format;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyFormatterTest {

    @Test
    void shouldFormatRupiahWithThousandSeparator() {
        assertEquals("Rp. 3,500,000.00", MoneyFormatter.rupiah(new BigDecimal("3500000")));
        assertEquals("Rp. 2,641,423.50", MoneyFormatter.rupiah(new BigDecimal("2641423.5")));
    }

    @Test
    void shouldFormatRateWithIndonesianDecimalComma() {
        assertEquals("8%", MoneyFormatter.percent(new BigDecimal("0.08")));
        assertEquals("8,1%", MoneyFormatter.percent(new BigDecimal("0.081")));
        assertEquals("8,6%", MoneyFormatter.percent(new BigDecimal("0.086")));
    }
}
