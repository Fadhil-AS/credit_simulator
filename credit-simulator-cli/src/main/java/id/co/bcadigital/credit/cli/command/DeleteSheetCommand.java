package id.co.bcadigital.credit.cli.command;

import id.co.bcadigital.credit.cli.presenter.SimulationPresenter;

import java.util.List;

public class DeleteSheetCommand implements Command {

    private final SimulationPresenter presenter;

    public DeleteSheetCommand(SimulationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String name() {
        return "delete";
    }

    @Override
    public String usage() {
        return "delete <nama-sheet>";
    }

    @Override
    public String description() {
        return "Menghapus sheet yang tersimpan.";
    }

    @Override
    public void execute(List<String> arguments) {
        presenter.deleteSheet(arguments.isEmpty() ? null : arguments.get(0));
    }
}
