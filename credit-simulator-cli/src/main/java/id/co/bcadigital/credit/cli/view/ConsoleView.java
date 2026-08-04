package id.co.bcadigital.credit.cli.view;

import id.co.bcadigital.credit.core.domain.InstallmentSchedule;

import java.util.List;

public interface ConsoleView {

    void showBanner();

    void showMessage(String message);

    void showError(String message);

    void showTable(String title, List<String> rows);

    void showSchedule(String title, InstallmentSchedule schedule);

    String prompt(String label);

    boolean hasMoreInput();

    boolean interactive();
}
