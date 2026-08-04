package id.co.bcadigital.credit.cli.presenter;

import id.co.bcadigital.credit.cli.CreditSimulatorApplication;
import id.co.bcadigital.credit.cli.io.ScriptedLineReader;
import id.co.bcadigital.credit.cli.remote.LoanApiClient;
import id.co.bcadigital.credit.cli.sheet.PayloadRepository;
import id.co.bcadigital.credit.cli.sheet.SheetRepository;
import id.co.bcadigital.credit.cli.view.SystemConsoleView;
import id.co.bcadigital.credit.core.calculation.DecliningBalanceInstallmentCalculator;
import id.co.bcadigital.credit.core.validation.LoanInputParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleControllerTest {

    @TempDir
    Path sheetDirectory;

    @Test
    @DisplayName("plain answer lines produce the installment table of the sample")
    void shouldSimulateFromScriptedAnswers() {
        String output = run(List.of("mobil", "bekas", "2023", "100000000", "3", "25000000", "exit"));

        assertTrue(output.contains("tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%"), output);
        assertTrue(output.contains("tahun 2 : Rp. 2,432,250.00/bln , Suku Bunga : 8,1%"), output);
        assertTrue(output.contains("tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%"), output);
    }

    @Test
    void shouldListEveryCommandOnShow() {
        String output = run(List.of("show", "exit"));

        for (String command : List.of("show", "simulate", "load", "save", "switch", "sheets", "delete", "exit")) {
            assertTrue(output.contains(command), "missing command " + command + " in:\n" + output);
        }
    }

    @Test
    @DisplayName("a saved sheet can be listed and reopened")
    void shouldSaveAndSwitchSheet() {
        String output = run(List.of(
                "motor", "bekas", "2020", "20000000", "2", "5000000",
                "save motor-bekas",
                "mobil", "baru", String.valueOf(java.time.Year.now().getValue()), "300000000", "4", "105000000",
                "save mobil-baru",
                "sheets",
                "switch motor-bekas",
                "exit"));

        assertTrue(output.contains("Sheet 'motor-bekas' berhasil disimpan."), output);
        assertTrue(output.contains("Sheet 'mobil-baru' berhasil disimpan."), output);
        assertTrue(output.contains("SHEET MOTOR-BEKAS"), output);
        assertTrue(output.contains("Jenis Kendaraan   : Motor"), output);
    }

    @Test
    void shouldRejectUnknownCommand() {
        String output = run(List.of("terbang", "exit"));

        assertTrue(output.contains("tidak dikenal"), output);
    }

    @Test
    @DisplayName("invalid scripted input stops the simulation instead of consuming the next answers")
    void shouldAbortSimulationOnInvalidScriptedInput() {
        String output = run(List.of("mobil", "baru", "1999", "exit"));

        assertTrue(output.contains("Kendaraan Baru tidak boleh lebih tua"), output);
        assertTrue(output.contains("Simulasi dibatalkan"), output);
        assertFalse(output.contains("HASIL SIMULASI KREDIT"), output);
    }

    @Test
    void shouldReportSaveWithoutSimulation() {
        String output = run(List.of("save draft", "exit"));

        assertTrue(output.contains("Belum ada hasil simulasi"), output);
    }

    private String run(List<String> script) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        SystemConsoleView view = new SystemConsoleView(out, new ScriptedLineReader(script, out));
        LoanInputParser parser = new LoanInputParser();
        SimulationPresenter presenter = new SimulationPresenter(
                view,
                new DecliningBalanceInstallmentCalculator(),
                parser,
                new SheetRepository(sheetDirectory),
                new PayloadRepository(sheetDirectory.resolve("mock")),
                new LoanApiClient(URI.create("http://localhost:1/unused"),
                        URI.create("http://localhost:1/unused"), parser));
        CreditSimulatorApplication.newController(view, presenter).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
