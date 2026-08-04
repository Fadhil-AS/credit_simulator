package id.co.bcadigital.credit.cli.command;

import id.co.bcadigital.credit.cli.presenter.SimulationPresenter;

import java.util.List;

public class SimulateCommand implements Command {

    private final SimulationPresenter presenter;

    public SimulateCommand(SimulationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String name() {
        return "simulate";
    }

    @Override
    public String usage() {
        return "simulate";
    }

    @Override
    public String description() {
        return "Menghitung cicilan kredit kendaraan dari input baru.";
    }

    @Override
    public List<String> aliases() {
        return List.of("hitung", "start");
    }

    @Override
    public void execute(List<String> arguments) {
        presenter.simulate(arguments.isEmpty() ? null : arguments.get(0));
    }
}
