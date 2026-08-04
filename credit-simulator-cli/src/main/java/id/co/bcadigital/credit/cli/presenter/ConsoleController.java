package id.co.bcadigital.credit.cli.presenter;

import id.co.bcadigital.credit.cli.command.Command;
import id.co.bcadigital.credit.cli.command.CommandRegistry;
import id.co.bcadigital.credit.cli.view.ConsoleView;
import id.co.bcadigital.credit.core.domain.VehicleType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConsoleController {

    private final ConsoleView view;
    private final CommandRegistry registry;
    private final SimulationPresenter presenter;

    public ConsoleController(ConsoleView view, CommandRegistry registry, SimulationPresenter presenter) {
        this.view = view;
        this.registry = registry;
        this.presenter = presenter;
    }

    public void run() {
        view.showBanner();
        while (presenter.isRunning() && view.hasMoreInput()) {
            String line = view.prompt("credit-simulator> ");
            if (line == null) {
                break;
            }
            dispatch(line.trim());
        }
    }

    private void dispatch(String line) {
        if (line.isEmpty()) {
            return;
        }
        List<String> tokens = Arrays.asList(line.split("\\s+"));
        String head = tokens.get(0);
        Optional<Command> command = registry.find(head);
        if (command.isPresent()) {
            executeSafely(command.get(), tokens.subList(1, tokens.size()));
            return;
        }
        if (VehicleType.parse(head) != null) {
            presenter.simulate(head);
            return;
        }
        view.showError("Perintah '" + head + "' tidak dikenal. Ketik 'show' untuk melihat daftar perintah.");
    }

    private void executeSafely(Command command, List<String> arguments) {
        try {
            command.execute(arguments);
        } catch (RuntimeException e) {
            view.showError(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }
}
