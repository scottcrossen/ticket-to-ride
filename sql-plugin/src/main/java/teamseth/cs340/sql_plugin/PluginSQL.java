package teamseth.cs340.sql_plugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import teamseth.cs340.common.models.server.ModelObjectType;
import teamseth.cs340.common.persistence.plugin.IPersistenceProvider;
import teamseth.cs340.common.util.Logger;
import teamseth.cs340.common.util.MaybeTuple;
import teamseth.cs340.sql_plugin.DataAccess.Connection;
import teamseth.cs340.sql_plugin.DataAccess.DatabaseException;
import teamseth.cs340.sql_plugin.DataAccess.SQLDAO;

public class PluginSQL implements IPersistenceProvider {

    SQLDAO sqlDAO;
    Map<UUID, Integer> orderMap = new HashMap<>();

    @Override
    public void initialize() {
        sqlDAO = new SQLDAO();
        try {
            Connection.SINGLETON.openConnection();
            sqlDAO.SINGLETON.createTables();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        Logger.info("SQL provider initialized");
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.SQL;
    }

    @Override
    public void finalize() {

    }

    @Override
    public CompletableFuture<Boolean> upsertObject(Serializable newObjectState, Serializable delta, UUID ObjectId, ModelObjectType type, int deltasBeforeUpdate) {
        if(orderMap.get(ObjectId).equals(null)) {
            orderMap.put(ObjectId, 1);
        }
        int count = orderMap.get(ObjectId);
        try {
            sqlDAO.SINGLETON.addDelta(delta, ObjectId, count);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        if(count == deltasBeforeUpdate) {
            orderMap.put(ObjectId, 1);
            try {
                sqlDAO.SINGLETON.clearDeltas();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
            //insert into object here based on uuid objecttype
            try {
                sqlDAO.SINGLETON.addObject(newObjectState, ObjectId, type);
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
            //remove deltas based on uuid
            try {
                sqlDAO.SINGLETON.removeDeltasBasedOnGame(ObjectId);
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
        else {
            count++;
            orderMap.put(ObjectId, count);
        }
        return CompletableFuture.supplyAsync(() -> false);
    }

    @Override
    public CompletableFuture<List<MaybeTuple<Serializable, List<Serializable>>>> getAllOfType(ModelObjectType type) {
        //get every object and delta based on the object (Serializable)
        return CompletableFuture.supplyAsync(() -> new LinkedList<MaybeTuple<Serializable, List<Serializable>>>());
    }
}
