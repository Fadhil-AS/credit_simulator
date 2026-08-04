package id.co.bcadigital.credit.cli.presenter;

import id.co.bcadigital.credit.cli.remote.LoanApiClient;
import id.co.bcadigital.credit.cli.remote.RemoteServiceException;
import id.co.bcadigital.credit.cli.sheet.PayloadRepository;
import id.co.bcadigital.credit.cli.sheet.SheetRepository;
import id.co.bcadigital.credit.cli.view.ConsoleView;
import id.co.bcadigital.credit.core.calculation.InstallmentCalculator;
import id.co.bcadigital.credit.core.domain.LoanApplication;
import id.co.bcadigital.credit.core.domain.VehicleCondition;
import id.co.bcadigital.credit.core.domain.VehicleFactory;
import id.co.bcadigital.credit.core.domain.VehicleType;
import id.co.bcadigital.credit.core.validation.LoanInputParser;
import id.co.bcadigital.credit.core.validation.ValidationException;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SimulationPresenter {

    private final ConsoleView view;
    private final InstallmentCalculator calculator;
    private final LoanInputParser parser;
    private final SheetRepository sheets;
    private final PayloadRepository payloads;
    private final LoanApiClient apiClient;

    private LoanApplication currentApplication;
    private String currentSheetName;
    private boolean running = true;

    public SimulationPresenter(ConsoleView view,
                               InstallmentCalculator calculator,
                               LoanInputParser parser,
                               SheetRepository sheets,
                               PayloadRepository payloads,
                               LoanApiClient apiClient) {
        this.view = view;
        this.calculator = calculator;
        this.parser = parser;
        this.sheets = sheets;
        this.payloads = payloads;
        this.apiClient = apiClient;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
        view.showMessage("Terima kasih telah menggunakan Credit Simulator.");
    }

    public void simulate(String presetVehicleType) {
        try {
            VehicleType type = presetVehicleType == null
                    ? ask("Jenis Kendaraan (Motor/Mobil) : ", parser::parseVehicleType)
                    : parser.parseVehicleType(presetVehicleType);
            VehicleCondition condition = ask("Kondisi Kendaraan (Baru/Bekas) : ", parser::parseVehicleCondition);
            int year = ask("Tahun Kendaraan (4 digit) : ", answer -> parser.parseVehicleYear(answer, condition));
            BigDecimal loanAmount = ask("Jumlah Pinjaman (maks 1000000000) : ", parser::parseLoanAmount);
            int tenure = ask("Tenor Pinjaman (1-6 tahun) : ", parser::parseTenure);
            BigDecimal downPayment = ask("Jumlah DP : ",
                    answer -> parser.parseDownPayment(answer, loanAmount, condition));

            LoanApplication application = new LoanApplication(
                    VehicleFactory.create(type, condition, year), loanAmount, tenure, downPayment);
            currentApplication = application;
            currentSheetName = null;
            display("HASIL SIMULASI KREDIT");
        } catch (InputAbortedException e) {
            view.showError(e.getMessage());
        } catch (ValidationException e) {
            view.showError(e.getMessage());
        }
    }

    public void loadFromWebService(String payloadName) {
        try {
            LoanApplication application;
            if (payloadName == null) {
                view.showMessage("Mengambil data dari " + apiClient.endpoint() + " ...");
                application = fetchWithFallback();
            } else {
                URI target = apiClient.resolvePayload(payloadName);
                view.showMessage("Mengambil data dari " + target + " ...");
                application = apiClient.fetchFrom(target);
            }
            currentApplication = application;
            currentSheetName = null;
            display("HASIL SIMULASI KREDIT (WEB SERVICE)");
        } catch (IllegalArgumentException | RemoteServiceException | ValidationException e) {
            view.showError(e.getMessage());
        }
    }

    public void saveSheet(String name) {
        if (currentApplication == null) {
            view.showError("Belum ada hasil simulasi yang dapat disimpan. Jalankan 'simulate' atau 'load' dahulu.");
            return;
        }
        String sheetName = name;
        if (sheetName == null) {
            sheetName = view.prompt("Nama Sheet : ");
        }
        try {
            sheets.save(sheetName, currentApplication);
            currentSheetName = sheetName.trim();
            view.showMessage("Sheet '" + currentSheetName + "' berhasil disimpan.");
            publishPayload(currentSheetName);
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        } catch (RuntimeException e) {
            view.showError("Gagal menyimpan sheet: " + e.getMessage());
        }
    }

    public void switchSheet(String name) {
        String sheetName = name;
        if (sheetName == null) {
            sheetName = view.prompt("Nama Sheet : ");
        }
        try {
            Optional<LoanApplication> application = sheets.find(sheetName);
            if (application.isEmpty()) {
                view.showError("Sheet '" + sheetName + "' tidak ditemukan. Gunakan 'sheets' untuk melihat daftar.");
                return;
            }
            currentApplication = application.get();
            currentSheetName = sheetName.trim();
            display("SHEET " + currentSheetName.toUpperCase());
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        } catch (RuntimeException e) {
            view.showError("Gagal membuka sheet: " + e.getMessage());
        }
    }

    public void listSheets() {
        List<String> names = sheets.names();
        if (names.isEmpty()) {
            view.showMessage("Belum ada sheet tersimpan.");
            return;
        }
        view.showTable("DAFTAR SHEET", names.stream()
                .map(name -> "  " + (name.equals(currentSheetName) ? "* " : "  ") + name)
                .toList());
    }

    public void deleteSheet(String name) {
        String sheetName = name;
        if (sheetName == null) {
            sheetName = view.prompt("Nama Sheet : ");
        }
        try {
            boolean sheetRemoved = sheets.delete(sheetName);
            boolean payloadRemoved = payloads.delete(sheetName);
            if (!sheetRemoved && !payloadRemoved) {
                view.showError("Sheet '" + sheetName + "' tidak ditemukan.");
                return;
            }
            if (sheetName.trim().equals(currentSheetName)) {
                currentSheetName = null;
            }
            view.showMessage("Sheet '" + sheetName.trim() + "' berhasil dihapus"
                    + (payloadRemoved ? " beserta payload web service-nya." : "."));
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        }
    }

    private void publishPayload(String name) {
        try {
            payloads.publish(name, currentApplication);
            view.showMessage("Payload web service siap, jalankan 'load " + name + "'.");
        } catch (RuntimeException e) {
            view.showError("Gagal menulis payload web service: " + e.getMessage());
        }
    }

    private LoanApplication fetchWithFallback() {
        try {
            return apiClient.fetchExistingCalculation();
        } catch (RemoteServiceException e) {
            view.showError(e.getMessage());
            view.showMessage("Mencoba endpoint cadangan " + apiClient.fallbackEndpoint() + " ...");
            return apiClient.fetchFrom(apiClient.fallbackEndpoint());
        }
    }

    private void display(String title) {
        view.showSchedule(title, calculator.calculate(currentApplication));
    }

    private <T> T ask(String label, Function<String, T> mapper) {
        while (true) {
            String answer = view.prompt(label);
            if (answer == null) {
                throw new InputAbortedException("Input tidak lengkap, simulasi dibatalkan.");
            }
            try {
                return mapper.apply(answer);
            } catch (ValidationException e) {
                view.showError(e.getMessage());
                if (!view.interactive()) {
                    throw new InputAbortedException("Simulasi dibatalkan karena input file tidak valid.");
                }
            }
        }
    }

    private static class InputAbortedException extends RuntimeException {
        InputAbortedException(String message) {
            super(message);
        }
    }
}
