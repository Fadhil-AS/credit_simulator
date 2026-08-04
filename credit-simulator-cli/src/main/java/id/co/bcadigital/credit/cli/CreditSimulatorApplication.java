package id.co.bcadigital.credit.cli;

import id.co.bcadigital.credit.cli.command.CommandRegistry;
import id.co.bcadigital.credit.cli.command.DeleteSheetCommand;
import id.co.bcadigital.credit.cli.command.ExitCommand;
import id.co.bcadigital.credit.cli.command.ListSheetsCommand;
import id.co.bcadigital.credit.cli.command.LoadCommand;
import id.co.bcadigital.credit.cli.command.SaveSheetCommand;
import id.co.bcadigital.credit.cli.command.ShowCommand;
import id.co.bcadigital.credit.cli.command.SimulateCommand;
import id.co.bcadigital.credit.cli.command.SwitchSheetCommand;
import id.co.bcadigital.credit.cli.io.InteractiveLineReader;
import id.co.bcadigital.credit.cli.io.LineReader;
import id.co.bcadigital.credit.cli.io.ScriptedLineReader;
import id.co.bcadigital.credit.cli.presenter.ConsoleController;
import id.co.bcadigital.credit.cli.presenter.SimulationPresenter;
import id.co.bcadigital.credit.cli.remote.LoanApiClient;
import id.co.bcadigital.credit.cli.sheet.PayloadRepository;
import id.co.bcadigital.credit.cli.sheet.SheetRepository;
import id.co.bcadigital.credit.cli.view.ConsoleView;
import id.co.bcadigital.credit.cli.view.SystemConsoleView;
import id.co.bcadigital.credit.core.calculation.DecliningBalanceInstallmentCalculator;
import id.co.bcadigital.credit.core.validation.LoanInputParser;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CreditSimulatorApplication {

    private CreditSimulatorApplication() {
    }

    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        if (args.length > 1) {
            out.println("Penggunaan: credit_simulator [file_inputs.txt]");
            System.exit(2);
        }
        LineReader reader;
        if (args.length == 1) {
            Path input = Path.of(args[0]);
            if (!Files.isReadable(input)) {
                out.println("[ERROR] File input tidak ditemukan: " + input);
                System.exit(2);
                return;
            }
            reader = ScriptedLineReader.fromFile(input, out);
        } else {
            reader = new InteractiveLineReader(System.in, out);
        }
        newController(new SystemConsoleView(out, reader)).run();
    }

    public static ConsoleController newController(ConsoleView view) {
        LoanInputParser parser = new LoanInputParser();
        SimulationPresenter presenter = new SimulationPresenter(
                view,
                new DecliningBalanceInstallmentCalculator(),
                parser,
                SheetRepository.defaultRepository(),
                PayloadRepository.defaultRepository(),
                LoanApiClient.withDefaultEndpoint(parser));
        return newController(view, presenter);
    }

    public static ConsoleController newController(ConsoleView view, SimulationPresenter presenter) {
        CommandRegistry registry = new CommandRegistry();
        registry.register(new ShowCommand(registry, view))
                .register(new SimulateCommand(presenter))
                .register(new LoadCommand(presenter))
                .register(new SaveSheetCommand(presenter))
                .register(new SwitchSheetCommand(presenter))
                .register(new ListSheetsCommand(presenter))
                .register(new DeleteSheetCommand(presenter))
                .register(new ExitCommand(presenter));
        return new ConsoleController(view, registry, presenter);
    }
}
