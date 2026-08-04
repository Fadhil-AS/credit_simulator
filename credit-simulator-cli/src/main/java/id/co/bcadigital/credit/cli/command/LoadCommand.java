package id.co.bcadigital.credit.cli.command;

import id.co.bcadigital.credit.cli.presenter.SimulationPresenter;

import java.util.List;

public class LoadCommand implements Command {

    private final SimulationPresenter presenter;

    public LoadCommand(SimulationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String name() {
        return "load";
    }

    @Override
    public String usage() {
        return "load <nama-file>";
    }

    @Override
    public String description() {
        return "Mengambil simulasi dari web service lalu menghitungnya otomatis.";
    }

    @Override
    public void execute(List<String> arguments) {
        presenter.loadFromWebService(arguments.isEmpty() ? null : arguments.get(0));
    }
}
