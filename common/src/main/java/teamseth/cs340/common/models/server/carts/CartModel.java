package teamseth.cs340.common.models.server.carts;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import teamseth.cs340.common.exceptions.ModelActionException;
import teamseth.cs340.common.exceptions.ResourceNotFoundException;
import teamseth.cs340.common.exceptions.UnauthorizedException;
import teamseth.cs340.common.models.server.IServerModel;
import teamseth.cs340.common.models.server.ModelObjectType;
import teamseth.cs340.common.persistence.PersistenceAccess;
import teamseth.cs340.common.util.auth.AuthAction;
import teamseth.cs340.common.util.auth.AuthToken;

/**
 * @author Scott Leland Crossen
 * @copyright (C) Copyright 2017 Scott Leland Crossen
 */
public class CartModel extends AuthAction implements IServerModel {
    private static CartModel instance;

    public static CartModel getInstance() {
        if(instance == null) {
            instance = new CartModel();
        }
        return instance;
    }

    private HashSet<CartSet> cartSets = new HashSet<>();

    public CompletableFuture<Boolean> loadAllFromPersistence() {
        CompletableFuture<List<CartSet>> persistentData = PersistenceAccess.getObjects(ModelObjectType.CARTS);
        return persistentData.thenApply((List<CartSet> newData) -> {
            cartSets.addAll(newData);
            return true;
        });
    }

    private CartSet getCartSet(UUID id) throws ResourceNotFoundException {
        for (CartSet cartSet : cartSets) {
            if(cartSet.getId().equals(id)) {
                return cartSet;
            }
        }
        throw new ResourceNotFoundException();
    }

    public int getPlayerCartsById(UUID cartId, UUID playerId, AuthToken token) throws ResourceNotFoundException, UnauthorizedException {
        AuthAction.user(token);
        return getCartSet(cartId).getPlayerCarts(playerId);
    }

    public int getPlayerCarts(UUID cartId, AuthToken token) throws ResourceNotFoundException, UnauthorizedException {
        AuthAction.user(token);
        UUID playerId = token.getUser();
        return getCartSet(cartId).getPlayerCarts(playerId);
    }

    public void decrementPlayerCarts(UUID cartId, int carts, AuthToken token) throws ResourceNotFoundException, UnauthorizedException {
        AuthAction.user(token);
        UUID playerId = token.getUser();
        getCartSet(cartId).decrementPlayerCarts(playerId, carts);
    }

    public void incrementPlayerCarts(UUID cartId, int carts, AuthToken token) throws ResourceNotFoundException, UnauthorizedException {
        AuthAction.user(token);
        UUID playerId = token.getUser();
        getCartSet(cartId).incrementPlayerCarts(playerId, carts);
    }

    public void upsert(CartSet newDeck, AuthToken token) throws UnauthorizedException, ModelActionException {
        AuthAction.user(token);
        try {
            getCartSet(newDeck.getId());
            throw new ModelActionException();
        } catch (ResourceNotFoundException e) {
            cartSets.add(newDeck);
        }
    }
}
