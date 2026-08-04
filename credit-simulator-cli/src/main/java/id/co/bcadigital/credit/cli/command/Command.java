package id.co.bcadigital.credit.cli.command;

import java.util.List;

public interface Command {

    String name();

    String usage();

    String description();

    void execute(List<String> arguments);

    default List<String> aliases() {
        return List.of();
    }
}
