package network.server;

import network.Protocol.Protocol;
import network.controller.CardActionCallback;
import network.controller.GameController;
import network.exceptions.ClientUnavailableException;
import network.model.Card;
import network.model.Player;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Server implements Runnable{
    /** The ServerSocket of this HotelServer */
    private ServerSocket ssock;

    private Object insertExplodeLock = new Object();

    /** List of ClientHandlers, one for each connected client */
    private List<ClientHandler> clients;

    /** Next client number, increasing for every new connection */
    private int nextClientNo;

    /** The view of this server */
    private ServerTUI serverTUI;

    /** Number of clients to play. Decided by the first connected client.
     * Initialised with 2 because there must be at least 2 players to start.*/
    private int numberOfPlayersToPlay = 2;

    /** Currently connected players counter */
    int playersConnectedCounter = 0;

    /** Communication which links game logic to Client */
    private GameController controller = null;

    /** Usage of how the Server config should be started made */
    private static final String USAGE
            = "usage: <port>";

    /* ************************************
                 CONSTRUCTOR
    ************************************ */

    /** Sets up a new Server and opens a new
     * ServerSocket at localhost on a user-defined port.
     * The user is asked to input a port, after which a socket is attempted
     * to be opened. If the attempt succeeds, the method ends, If the
     * attempt fails, the user decides to try again, after which an
     * ExitProgram exception is thrown or a new port is entered.
     *
     * @throws IOException if a connection can not be created on the given
     *                     port.
     * @ensures a serverSocket is opened.
     */
    public Server(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println(USAGE);
            System.exit(0);
        }

        clients = new ArrayList<>();
        serverTUI = new ServerTUI();
        nextClientNo = 0; // Array starts at 0
        ssock = null;
        int port = 0;

        // parse args[0] - the port
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            serverTUI.showMessage(USAGE);
            serverTUI.showMessage("ERROR: port " + args[2]
                    + " is not an integer");
            System.exit(0);
        }

        try {
            serverTUI.showMessage("Attempting to open a socket at 127.0.0.1 "
                    + "on port " + port + ".");
            ssock = new ServerSocket(1234, 0,
                    InetAddress.getByName("127.0.0.1"));
            serverTUI.showMessage("Server started on port " + port + ". ");
        } catch (IOException e) {
            serverTUI.showMessage("ERROR: could not create a socket on "
                    + "127.0.0.1" + " and port " + port + ".");
        }
    }

    /* ************************************
              GETTER'S & SETTER'S
    ************************************ */

    /**
     * Provides the list of clients.
     * @ensures this.clients != null
     * @return this.clients
     */
    public List<ClientHandler> getClients() {
        return this.clients;
    }

    public void setNextClientNo(int no) {
        this.nextClientNo += no;
    }

    public void setNumberOfPlayersToPlay(int numberOfPlayersToPlay) {
        this.numberOfPlayersToPlay = numberOfPlayersToPlay;
    }

    public void setNumberOfCurrentPlayers(int value) { this.playersConnectedCounter += value; }

    public Object getClientInsertExplodeLock(int clientHandlerIndex) {
        return clients.get(clientHandlerIndex).getInsertExplodeLock();
    }

    /* ************************************
                    NETWORK
    ************************************ */

    public void removePlayerFromGame(ClientHandler handler) {
        controller.removePlayerFromGame(clients.indexOf(handler));
    }

    /**
     * Opens a new server socket and starts a new
     * ClientHandler for every connecting client. Asks the first client
     * who is connected for the number of players that should be in the game.
     */
    @Override
    public void run() {
        boolean openNewSocket = true;
        while (openNewSocket) {
            try {
                while (playersConnectedCounter < numberOfPlayersToPlay) {
                    Socket sock = ssock.accept();
                    playersConnectedCounter++;
                    String name = "Client "
                            + String.format("%02d", nextClientNo++);
                    serverTUI.showMessage("New client [" + name + "] connected!");
                    ClientHandler handler =
                            new ClientHandler(sock, this);
                    handler.handleHello();
                    clients.add(handler);

                    if (clients.size() == 1 && clients.get(0).isSupportsLobby()) {
                        handler.handleLobbySize();
                    }

                    new Thread(handler, handler.getName()).start();
                    serverTUI.showMessage("Client [" +
                            clients.get(clients.size() - 1).getName() +
                            "] put into lobby!");

                    if (playersConnectedCounter == numberOfPlayersToPlay) {
                        try {
                            ssock.close();
                            this.startGame();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            } catch (Exception e) {
                serverTUI.showMessage("Error accepting the client and starting a " +
                        "client handler\n" + e.getMessage());
                e.printStackTrace();
                playersConnectedCounter--;
            }
            openNewSocket = false;
        }
    }

    /**
     * Allows method to accept any number of card arguments. Useful
     * because the logic does not differ that much. It is just that
     * one can play:
     * <p> 1 card - EX: {@link Protocol.cardType#FUTURE} </p>
     * <p> 2 cards - EX: {@link Protocol.cardType#POTATO}</p>
     * <p> combinations - EX: {@link Protocol.cardType#ATTACK}
     *      *      && a player's name </p>
     * @param cmd != null
     */
    public void doPlayMove(String[] cmd, ClientHandler handler) {
        // Translation between clientHandler and Player is doing with ArrayLists' indexes
        controller.doPlayMove(cmd, clients.indexOf(handler));
    }

    public void doInsertExplode(int cardIndex, ClientHandler clientHandler) {
        controller.doInsertExplode(cardIndex, clients.indexOf(clientHandler));
    }

    /**
     * Sends a broadcast to all players.
     * @param message
     */
    public void doBroadcast(String message) {
        for (ClientHandler client : clients) {
            try {
                client.sendMessage(Protocol.BROADCAST + Protocol.DELIMITER
                        + message);
            } catch (ClientUnavailableException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Sends a broadcast to all players, except for the one who sent
     * the command to the server, just like in a chat environment.
     * @param message
     * @param clientHandler
     */
    public void doChatBroadcast(String message, ClientHandler clientHandler) {
        for (ClientHandler client : clients) {
            try {
                if (! client.equals(clientHandler)) {
                    client.sendMessage(Protocol.BROADCAST + Protocol.DELIMITER
                            + "[" + clientHandler.getName() + "]: " + message);
                }
            } catch (ClientUnavailableException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * Sends a broadcast to all players, except for the one who sent
     * the command to the server, just like in a chat environment.
     * @param message
     * @param clientHandler
     */
    public void doChatBroadcast(String message, int playerIndex) {
        for (ClientHandler client : clients) {
            try {
                if ( clients.indexOf(client) != playerIndex) {
                    client.sendMessage(Protocol.BROADCAST + Protocol.DELIMITER
                            + "[" + clients.get(playerIndex).getName() + "]: " + message);
                }
            } catch (ClientUnavailableException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Change from player to clientHandler happens via clientHandlerIndex,
     * which is just player's index in the {@link network.model.Game#players}*/
    public void doPrivate(String msg, int clientHandlerIndex) {
        try {
            clients.get(clientHandlerIndex)
                    .sendMessage(Protocol.PRIVATE + Protocol.DELIMITER + msg);
        } catch (ClientUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void doKick(String msg, int clientHandlerIndex) {
        this.doPrivate(msg, clientHandlerIndex);
        clients.get(clientHandlerIndex).shutdown();
    }


    public void resumeGame() {
        controller.resumeGame();
    }

    public void revealTopThreeCardsToPlayer(
            String lastCardName,
            String secondToLastCardName,
            String thirdToLastCardName,
            int clientIndexToReplyTo
    ) {
        String msgToSend =
                Protocol.SEETHEFUTURE + Protocol.DELIMITER +
                        lastCardName + Protocol.DELIMITER +
                        secondToLastCardName + Protocol.DELIMITER +
                        thirdToLastCardName;
        try {
            clients.get(clientIndexToReplyTo).sendMessage(msgToSend);
        } catch (ClientUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    /* ************************************
                    GENERAL
    ************************************ */

    public void askPlayerForCard(
            int targetPlayerIndex,
            String requestingPlayerName
    ) {
        this.doPrivate(requestingPlayerName + " has played a FAVOR to you." +
                " Type <play> <CARD's NAME>, just like a normal move.", targetPlayerIndex);
        clients.get(targetPlayerIndex).setAwaitFavorResponse(true);
    }

    /**
     * Removes a clientHandler from the client list.
     * @requires client != null
     */
    public void removeClient(ClientHandler client) {
        this.clients.remove(client);
    }

    /**
     * Verifies if the name obeys the protocol's rules, except if the
     * name is already taken by someone else.
     * @requires clientName != null && !clientName.isEmpty()
     * @param clientName
     * @return boolean
     * @ensures clientName obeys the protocol's rules or not.
     */
    public boolean isNameValid(String clientName) {
        boolean isNameServerCommand = (
                clientName.equals(Protocol.ANNOUNCE) ||
                        clientName.equals(Protocol.WELCOME) ||
                        clientName.equals(Protocol.REQUESTGAME) ||
                        clientName.equals(Protocol.START) ||
                        clientName.equals(Protocol.DRAW) ||
                        clientName.equals(Protocol.SEETHEFUTURE) ||
                        clientName.equals(Protocol.NEXTTURN) ||
                        clientName.equals(Protocol.GAMEOVER) ||
                        clientName.equals(Protocol.PLAYMOVE) ||
                        clientName.equals(Protocol.INSERTEXPLODE) ||
                        clientName.equals(Protocol.ABORT) ||
                        clientName.equals(Protocol.BROADCAST) ||
                        clientName.equals(Protocol.PRIVATE) ||
                        clientName.equals(Protocol.ERROR));

        boolean isNameCard = (
                Arrays.stream(Card.values())
                        .anyMatch(card -> card.name().equals(clientName)));

        return (!clientName.isEmpty() &&
                clientName.length() <= Protocol.MAX_USERNAME_CHARACTERS &&
                clientName.matches("^[a-zA-Z]+$") &&
                !(clientName.contains(Pattern.quote(Protocol.DELIMITER))) &&
                !isNameServerCommand &&
                !isNameCard);
    }

    /**
     * Verifies if the name is already taken by another client.
     * @requires clientName != null && !clientName.isEmpty()
     * @param clientName
     * @return boolean
     * @ensures clientName is taken or not.
     */
    public boolean isNameTaken(String clientName) {
        return (clients.stream().anyMatch(
                handler -> handler.getName().equals(clientName)));
    }

    public void startGame() {
        this.doBroadcast("Game has reached " + numberOfPlayersToPlay +
                " players. Game is starting now.");
        serverTUI.showMessage("Game has reached " + numberOfPlayersToPlay +
                " players. Game is starting now.");
        controller = (new GameController(clients, this));
        controller.startGame();
    }

    public boolean isClientsTurn(ClientHandler handler) {
        return controller.isClientsTurn(clients.indexOf(handler));
    }

    public void doFavorResponse(String card, ClientHandler targetClientHandler) {
        controller.doFavorResponse(card, clients.indexOf(targetClientHandler));
    }

    public void checkGameHasWinner() {
        controller.checkGameHasWinner();
    }

    public void removeCardFromPlayerHand(String[] clientMsgSplitted) {
        controller.removeCardFromPlayerHand(clientMsgSplitted);
    }

    /* ************************************
                      MAIN
    ************************************ */
    public static void main(String[] args) throws IOException {
        System.out.println("> Server is starting...");
        Server server = new Server(args);
        new Thread(server, "Server").start();
    }
}
