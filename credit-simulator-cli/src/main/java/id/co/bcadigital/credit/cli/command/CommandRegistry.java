package id.co.bcadigital.credit.cli.command;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CommandRegistry {

    private final Map<String, Command> byName = new LinkedHashMap<>();
    private final Map<String, Command> byAlias = new LinkedHashMap<>();

    public CommandRegistry register(Command command) {
        byName.put(command.name().toLowerCase(Locale.ROOT), command);
        for (String alias : command.aliases()) {
            byAlias.put(alias.toLowerCase(Locale.ROOT), command);
        }
        return this;
    }

    public Optional<Command> find(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String key = name.trim().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(byName.getOrDefault(key, byAlias.get(key)));
    }

    public Collection<Command> commands() {
        return List.copyOf(byName.values());
    }
}
