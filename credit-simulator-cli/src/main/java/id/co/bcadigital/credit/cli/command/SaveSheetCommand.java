package id.co.bcadigital.credit.cli.command;

import id.co.bcadigital.credit.cli.presenter.SimulationPresenter;

import java.util.List;

public class SaveSheetCommand implements Command {

    private final SimulationPresenter presenter;

    public SaveSheetCommand(SimulationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String name() {
        return "save";
    }

    @Override
    public String usage() {
        return "save <nama-sheet>";
    }

    @Override
    public String description() {
        return "Menyimpan hasil simulasi aktif ke sebuah sheet.";
    }

    @Override
    public void execute(List<String> arguments) {
        presenter.saveSheet(arguments.isEmpty() ? null : arguments.get(0));
    }
}
