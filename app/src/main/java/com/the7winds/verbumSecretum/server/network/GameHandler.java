package com.the7winds.verbumSecretum.server.network;

/**
 * Created by the7winds on 05.12.15.
 */
public class GameHandler {

    /*
        private void playGame() {
        for (;!game.isFinished();) {
            broadcast(new ServerMessages.GameState( game.getCurrent(),
                    game.getActivePlayers(),
                    game.getCardsThatShouldBeShowed()));

            if (!receivedPlayersMessages.isEmpty()) {
                ServerUtils.MessagePair messagePair = receivedPlayersMessages.remove();
                String id = messagePair.id;
                Message message = messagePair.message;

                if (game.isActive(id) && message.getClass().equals(PlayerMessages.Leave.class)) {
                    terminateGame();
                } else if (!game.isActive(id) && message.getClass().equals(PlayerMessages.Leave.class)) {
                    connectionHandlerMap.get(id).interrupt();
                    playersMap.remove(id);
                } else {
                    Assert.assertEquals(message.getClass(), PlayerMessages.Move.class);
                    Game.Move move = ((PlayerMessages.Move) message).getMove();
                    if (game.testMove(move)) {
                        game.applyMove(move);
                    } else {
                        sendTo(id, new ServerMessages.InvalidMove());
                    }
                }
            }
        }

        broadcast(new ServerMessages.GameState( game.getCurrent(),
                game.getActivePlayers(),
                game.getCardsThatShouldBeShowed()));
    }




    private void startGame() {
        broadcast(new ServerMessages.GameStarting());

        while (!receivedPlayersMessages.isEmpty()) {
            ServerUtils.MessagePair messagePair = receivedPlayersMessages.remove();
            String id = messagePair.id;
            Message message = messagePair.message;
            if (playersMap.containsKey(id)) {
                if (message.getClass().equals(PlayerMessages.Leave.class)) {
                    terminateGame();
                }
            }
        }

        game = new Game(playersMap);

        broadcast(new ServerMessages.GameStart(game.getActivePlayers()));
    }
     */
}
