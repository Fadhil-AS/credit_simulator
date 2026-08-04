package id.co.bcadigital.credit.cli.command;

import id.co.bcadigital.credit.cli.presenter.SimulationPresenter;

import java.util.List;

public class ExitCommand implements Command {

    private final SimulationPresenter presenter;

    public ExitCommand(SimulationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public String usage() {
        return "exit";
    }

    @Override
    public String description() {
        return "Keluar dari aplikasi.";
    }

    @Override
    public List<String> aliases() {
        return List.of("quit", "keluar");
    }

    @Override
    public void execute(List<String> arguments) {
        presenter.stop();
    }
}
