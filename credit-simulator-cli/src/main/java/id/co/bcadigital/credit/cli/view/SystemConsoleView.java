package id.co.bcadigital.credit.cli.view;

import id.co.bcadigital.credit.cli.io.LineReader;
import id.co.bcadigital.credit.core.domain.InstallmentSchedule;
import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.YearlyInstallment;
import id.co.bcadigital.credit.core.format.MoneyFormatter;

import java.io.PrintStream;
import java.util.List;

public class SystemConsoleView implements ConsoleView {

    private static final String SEPARATOR = "=".repeat(64);

    private final PrintStream out;
    private final LineReader reader;

    public SystemConsoleView(PrintStream out, LineReader reader) {
        this.out = out;
        this.reader = reader;
    }

    @Override
    public void showBanner() {
        out.println(SEPARATOR);
        out.println(" CREDIT SIMULATOR - Simulasi Cicilan Kredit Kendaraan");
        out.println(" Ketik 'show' untuk melihat seluruh perintah yang tersedia.");
        out.println(SEPARATOR);
    }

    @Override
    public void showMessage(String message) {
        out.println(message);
    }

    @Override
    public void showError(String message) {
        out.println("[ERROR] " + message);
    }

    @Override
    public void showTable(String title, List<String> rows) {
        out.println();
        out.println(title);
        out.println("-".repeat(Math.max(title.length(), 20)));
        rows.forEach(out::println);
        out.println();
    }

    @Override
    public void showSchedule(String title, InstallmentSchedule schedule) {
        LoanApplication application = schedule.application();
        out.println();
        out.println(SEPARATOR);
        out.println(title);
        out.println(SEPARATOR);
        out.println("Jenis Kendaraan   : " + application.vehicle().type().displayName());
        out.println("Kondisi Kendaraan : " + application.vehicle().condition().displayName());
        out.println("Tahun Kendaraan   : " + application.vehicle().year());
        out.println("Jumlah Pinjaman   : " + MoneyFormatter.rupiah(application.loanAmount()));
        out.println("Jumlah DP         : " + MoneyFormatter.rupiah(application.downPayment()));
        out.println("Pokok Pinjaman    : " + MoneyFormatter.rupiah(application.principal()));
        out.println("Tenor Pinjaman    : " + application.tenureYears() + " tahun");
        out.println("-".repeat(SEPARATOR.length()));
        for (YearlyInstallment installment : schedule.installments()) {
            out.println("tahun " + installment.year() + " : "
                    + MoneyFormatter.rupiah(installment.monthlyInstallment()) + "/bln"
                    + " , Suku Bunga : " + MoneyFormatter.percent(installment.interestRate()));
        }
        out.println("-".repeat(SEPARATOR.length()));
        out.println("Rata-rata Cicilan : " + MoneyFormatter.rupiah(schedule.averageMonthlyInstallment()) + "/bln");
        out.println("Total Pembayaran  : " + MoneyFormatter.rupiah(schedule.totalPayment()));
        out.println("Total Bunga       : " + MoneyFormatter.rupiah(schedule.totalInterest()));
        out.println(SEPARATOR);
        out.println();
    }

    @Override
    public String prompt(String label) {
        return reader.readLine(label);
    }

    @Override
    public boolean hasMoreInput() {
        return reader.hasNext();
    }

    @Override
    public boolean interactive() {
        return reader.interactive();
    }
}
