package teamseth.cs340.tickettoride.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.util.Observable;
import java.util.Observer;

import teamseth.cs340.common.commands.server.GetGameCommand;
import teamseth.cs340.common.exceptions.ResourceNotFoundException;
import teamseth.cs340.common.models.client.ClientModelRoot;
import teamseth.cs340.common.models.server.games.Game;
import teamseth.cs340.common.models.server.games.GameState;
import teamseth.cs340.tickettoride.R;
import teamseth.cs340.tickettoride.communicator.Poller;
import teamseth.cs340.tickettoride.fragment.GameLobbyFragment;
import teamseth.cs340.tickettoride.util.Toaster;

/**
 * @author Scott Leland Crossen
 * @Copyright 2017 Scott Leland Crossen
 */
public class GameLobbyActivity extends AppCompatActivity implements Observer {

    Game activeGame;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_lobby);
        ClientModelRoot.getInstance().games.addObserver(this);

        try {
            Poller.getInstance(this.getApplicationContext()).addToJobs(new GetGameCommand(ClientModelRoot.getInstance().games.getActive().getId()));
        } catch (Exception e) {
            startActivity(new Intent(this, GameListActivity.class));
        }

        try {
            activeGame = ClientModelRoot.getInstance().games.getActive();
        } catch (Exception e) {
            startActivity(new Intent(getApplicationContext(), GameListActivity.class));
        }

        FragmentManager fm = getSupportFragmentManager();
        fragment = fm.findFragmentById(R.id.LobbyFragment);

        if (fragment == null) {
            fragment = new GameLobbyFragment();
            fm.beginTransaction()
                    .add(R.id.LobbyFragment, fragment)
                    .commit();
        }
    }


    public void updateGame() {
        try {
            if (!ClientModelRoot.getInstance().games.getActive().getPlayers().equals(this.activeGame.getPlayers())) {
                System.out.println("Game updating");
                this.activeGame = ClientModelRoot.getInstance().games.getActive();
                ((GameLobbyFragment) fragment).setFields(activeGame);
            }
        } catch (ResourceNotFoundException e) {
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            if (!ClientModelRoot.getInstance().games.hasActive()) {
                Poller.getInstance(this.getApplicationContext()).reset();
                startActivity(new Intent(this, GameListActivity.class));
            } else if (ClientModelRoot.getInstance().games.hasActive() && ClientModelRoot.getInstance().games.getActive().getState().equals(GameState.START)) {
                Poller.getInstance(this.getApplicationContext()).reset();
                Context context = this.getApplicationContext();
                this.runOnUiThread(() -> Toaster.getInstance().makeToast(context, "New game started."));
            } else {
                this.runOnUiThread(() -> updateGame());
            }
        } catch (ResourceNotFoundException e) {
        }
    }
}
