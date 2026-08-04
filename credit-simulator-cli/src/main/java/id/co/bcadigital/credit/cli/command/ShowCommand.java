package id.co.bcadigital.credit.cli.command;

import id.co.bcadigital.credit.cli.view.ConsoleView;

import java.util.ArrayList;
import java.util.List;

public class ShowCommand implements Command {

    private final CommandRegistry registry;
    private final ConsoleView view;

    public ShowCommand(CommandRegistry registry, ConsoleView view) {
        this.registry = registry;
        this.view = view;
    }

    @Override
    public String name() {
        return "show";
    }

    @Override
    public String usage() {
        return "show";
    }

    @Override
    public String description() {
        return "Menampilkan seluruh perintah yang dapat digunakan.";
    }

    @Override
    public List<String> aliases() {
        return List.of("help", "menu");
    }

    @Override
    public void execute(List<String> arguments) {
        List<String> rows = new ArrayList<>();
        for (Command command : registry.commands()) {
            String aliases = command.aliases().isEmpty() ? "" : " (alias: " + String.join(", ", command.aliases()) + ")";
            rows.add(String.format("  %-22s %s%s", command.usage(), command.description(), aliases));
        }
        view.showTable("DAFTAR PERINTAH", rows);
    }
}
