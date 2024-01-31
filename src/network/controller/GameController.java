package network.controller;

import network.model.*;
import network.server.ClientHandler;
import network.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameController {
    private static Scanner userInput = new Scanner(System.in);
    private Game game;
    private ArrayList<Player> players;
    private Player currentPlayer;
    /** Used for communicating to Server -> ClientHandler -> Client */
    private Server server;
    private final Object gameLock = new Object();

    private String move = "";
    private int currentPlayerIndex;
    private boolean awaitingFavorResponse = false;

    private static final String FIRST_MOVE_INFO =
            "You make the first move!";

    /* ************************************
                  CONSTRUCTOR
    ************************************ */

    public GameController(List<ClientHandler> clients, Server server) {

        players = new ArrayList<>();
        for (int index = 0; index < clients.size(); index++) {
            players.add(new HumanPlayer(clients.get(index).getName()));
        }

        this.server = server;
        game = new Game(players, this);
        game.init();
    }

    /* ************************************
                    GENERAL
    ************************************ */

    public void startGame() {
        this.printHasFirstMove(game.getCurrentPlayerTurnIndex());
        game.getCurrentPlayer().setTurnsToPlay(1);
        this.promptDiscardPile("None yet.");
        this.printHandAllPlayers();

        while (!game.hasWinner()) {
            currentPlayer = game.getCurrentPlayer();
            currentPlayerIndex = game.getPlayers().indexOf(currentPlayer);

            synchronized (gameLock) {
                try {
                    gameLock.wait();
                    this.showDiscardPile();
                    if (!game.getCurrentPlayer().hasTurnsToPlay()) {
                        game.incrementPlayerTurnIndex();
                        this.informCurrentTurn(game.getCurrentPlayerTurnIndex());
                        server.doChatBroadcast("It's now his/her turn.",
                        game.getCurrentPlayerTurnIndex());
                    } else {
                        server.doChatBroadcast("It's still his/her turn.",
                                game.getCurrentPlayerTurnIndex());
                        this.sendPrivateMsg("It's still your turn",
                                game.getCurrentPlayerTurnIndex());
                    }
                    this.printHandAllPlayers();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Game loop interrupted.");
                }
            }
        }
    }

    public void showDiscardPile() {
        Card lastDiscardPileCard;
        if (!game.getDiscardPile().isEmpty()) {
            lastDiscardPileCard = game.getDiscardPile().get(
                    game.getDiscardPile().size() - 1);
            this.promptDiscardPile(lastDiscardPileCard.name());
        } else {
            this.promptDiscardPile("None yet.");
        }
    }

    /** Receives cards OR cards with target players
     *
     * @param cmd
     */
    public void doPlayMove(String[] cmd, int clientIndex) {
        game.processPlay(cmd);
    }

    public synchronized void doInsertExplode(int cardIndex, int playerIndex) {
        System.out.println("HERE ");
        game.doInsertExplode(cardIndex, playerIndex);
    }

    public void resumeGame() {
        synchronized (gameLock) {
            gameLock.notifyAll();
        }
    }

    public void askIndexOfReinsertingExplode() {
        int lastDeckCardIndex = game.getDeckObject().getDeckArray().size() - 1;
        server.doPrivate("You drew an EXPLODE, but you had a DEFUSE." +
                        " Specify an reinserting index between 0 and " +
                        lastDeckCardIndex + ". Type <insert> <INDEX>.",
                game.getCurrentPlayerTurnIndex());
    }

    public boolean isClientsTurn(int playerIndexWhoPlayedMove) {
        return game.isClientsTurn(playerIndexWhoPlayedMove);
    }

    public void promptLastThreeCardsFromDeck(
            Card lastCard,
            Card secondToLastCard,
            Card thirdToLastCard
    ) {
        server.revealTopThreeCardsToPlayer(
                lastCard.name(),
                secondToLastCard.name(),
                thirdToLastCard.name(),
                game.getCurrentPlayerTurnIndex()
        );
    }

    public void removeCardFromPlayerHand(String[] clientMsgSplitted) {
        game.removeCardFromPlayerHand(clientMsgSplitted);
    }

    /********** ***************  FROM TUI *************** **/

    public void promptDiscardPile(String lastDiscardPileCard) {
        server.doBroadcast("Last discard pile card : " + lastDiscardPileCard);
    }

    public void informCurrentTurn(int currentPlayerIndex) {
        server.doPrivate("It's your turn now!", currentPlayerIndex);
    }

    public void doBroadcast(String msg) {
        server.doBroadcast(msg);
    }

    public void printHandAllPlayers() {
        game.getPlayers().forEach(player -> this.promptPlayerHand(
                player.getName(),
                player.getHand(),
                game.getPlayers().indexOf(player)));
    }

    public void promptPlayerHand(String playerName,
                                 ArrayList<Card> playerHand,
                                 int currentPlayerIndex) {
        server.doPrivate("Your hand: " + playerHand, currentPlayerIndex);
    }

    public void printHasFirstMove(int indexPlayerFirstMove) {
        server.doChatBroadcast(
                "makes the first move", indexPlayerFirstMove);
        server.doPrivate(FIRST_MOVE_INFO, indexPlayerFirstMove);
    }

    public void sendPrivateMsg(String msg, int currentPlayerIndex) {
        server.doPrivate(msg, currentPlayerIndex);
    }

    public void announceKick(int currentPlayerIndex) {
        server.doKick("You drew an EXPLODE, but you didn't have" +
                "a DEFUSE. You are out.", currentPlayerIndex);
    }

    public void doFavorResponse(String card, int targetPlayerIndex) {
        game.doFavorResponse(card, targetPlayerIndex);
    }

    /* ************************************
                OVERRIDE METHODS
    ************************************ */

    public void requestCardFromPlayer(
            int targetPlayerIndex,
            String requestingPlayerName) {
        server.askPlayerForCard(targetPlayerIndex,
                requestingPlayerName);
    }

    public void removePlayerFromGame(int clientHandlerIndex) {
        game.removePlayerFromGame(clientHandlerIndex);
    }

    public void checkGameHasWinner() {
        game.checkHasWinner();
    }

}
