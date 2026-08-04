package id.co.bcadigital.credit.cli.command;

import id.co.bcadigital.credit.cli.presenter.SimulationPresenter;

import java.util.List;

public class ListSheetsCommand implements Command {

    private final SimulationPresenter presenter;

    public ListSheetsCommand(SimulationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String name() {
        return "sheets";
    }

    @Override
    public String usage() {
        return "sheets";
    }

    @Override
    public String description() {
        return "Menampilkan seluruh sheet yang tersimpan.";
    }

    @Override
    public void execute(List<String> arguments) {
        presenter.listSheets();
    }
}
