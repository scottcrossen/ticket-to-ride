package teamseth.cs340.common.models.client.games;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.UUID;

import teamseth.cs340.common.exceptions.ResourceNotFoundException;
import teamseth.cs340.common.models.IModel;
import teamseth.cs340.common.models.server.games.Game;

/**
 * @author Scott Leland Crossen
 * @Copyright 2017 Scott Leland Crossen
 */
public class GameModel extends Observable implements IModel<Game> {
    private static GameModel instance;

    public static GameModel getInstance() {
        if(instance == null) {
            instance = new GameModel();
        }
        return instance;
    }

    private HashSet<Game> games = new HashSet<Game>();
    private UUID active = null;

    public void upsert(Set<Game> newGames) {
        // Overwrite old data with new data on uuid.
        Iterator<Game> iterator1 = games.iterator();
        while (iterator1.hasNext()) {
            Game currentGame1 = iterator1.next();
            Iterator<Game> iterator2 = games.iterator();
            while (iterator2.hasNext()) {
                Game currentGame2 = iterator2.next();
                if (currentGame1.getId() == currentGame2.getId()) {
                    currentGame1 = currentGame2;
                    iterator2.remove();
                }
            }
        }
        // Add all the remaining.
        games.addAll(newGames);
        setChanged();
        notifyObservers();
    }

    public HashSet<Game> getAll(){
        return games;
    }

    public void setActive(Game current) {
        HashSet<Game> toAdd = new HashSet<Game>();
        toAdd.add(current);
        this.upsert(toAdd);

        this.active = current.getId();
        setChanged();
        notifyObservers();
    }

    public Game getActive() throws ResourceNotFoundException {
        if (active == null) throw new ResourceNotFoundException();
        return games.stream().filter(game -> game.getId() == active).findFirst().orElseThrow(() -> new ResourceNotFoundException());
    }

    public boolean hasActive() {
        return (active != null);
    }
}
