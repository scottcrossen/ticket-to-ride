package teamseth.cs340.common.models.client.history;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import teamseth.cs340.common.commands.client.IHistoricalCommand;
import teamseth.cs340.common.exceptions.ResourceNotFoundException;
import teamseth.cs340.common.models.client.ClientModelRoot;

/**
 * @author Scott Leland Crossen
 * @Copyright 2017 Scott Leland Crossen
 */
public class CommandHistory {
    private static CommandHistory instance;

    public static CommandHistory getInstance() {
        if(instance == null) {
            instance = new CommandHistory();
        }
        return instance;
    }

    private List<IHistoricalCommand> history = new LinkedList<>();

    public void add(IHistoricalCommand command) {
        history.add(command);
    }

    public List<String> getHistory() throws ResourceNotFoundException {
        Map<UUID, String> playerNames = ClientModelRoot.games.getActive().getPlayerNames();
        List<String> output = history.stream().map(command -> playerNames.get(command.playerOwnedby()) + " " + command.getDescription()).collect(Collectors.toList());
        return output;
    }

    public Optional<UUID> getLastId() {
        if (history.size() > 0) {
            return Optional.of(history.get(history.size() - 1)).map((IHistoricalCommand command) -> command.getId());
        } else {
            return Optional.empty();
        }
    }
}
