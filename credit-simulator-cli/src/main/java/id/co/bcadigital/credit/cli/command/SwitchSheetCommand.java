package id.co.bcadigital.credit.cli.command;

import id.co.bcadigital.credit.cli.presenter.SimulationPresenter;

import java.util.List;

public class SwitchSheetCommand implements Command {

    private final SimulationPresenter presenter;

    public SwitchSheetCommand(SimulationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String name() {
        return "switch";
    }

    @Override
    public String usage() {
        return "switch <nama-sheet>";
    }

    @Override
    public String description() {
        return "Berpindah ke sheet lain dan menampilkan ulang hasil perhitungannya.";
    }

    @Override
    public List<String> aliases() {
        return List.of("sheet");
    }

    @Override
    public void execute(List<String> arguments) {
        presenter.switchSheet(arguments.isEmpty() ? null : arguments.get(0));
    }
}
