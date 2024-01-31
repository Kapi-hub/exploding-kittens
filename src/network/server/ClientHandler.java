package network.server;

import network.Protocol.Protocol;
import network.exceptions.ClientUnavailableException;
import network.exceptions.ServerUnavailableException;

import java.io.*;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {

    /** Socket's Input and Output streams */
    private BufferedReader in;
    private BufferedWriter out;
    private Socket sock;

    /** Connected Server */
    private Server server;

    /** Name of this ClientHandler (name of the connected Client) */
    private String name;

    /** Name of what features does this instance of client handler
     *  supports. */
    private boolean supportsChat = false;
    private boolean supportsLobby = false;
    private boolean supportsCombos = false;
    private boolean awaitFavorResponse = false;

    private Object insertExplodeLock = new Object();
    private static Timer nopeTimer;
    private static int nopeCardsCounter;
    private static boolean timerActivated = false;
    private static int resumeGameHasBeenCalled = 0;
    private static String[] initialSequence;

    private static int dummyCounter = 0;
    /** A welcoming message that is sent upon connecting to the server. */
    private final static String WELCOME_MESSAGE =
            "Welcome to the server. You have been put in a lobby.";

    /** Boolean needed such that the run() method does not throw an error */
    private Boolean isShuttingDown = false;
    /* ************************************
                  CONSTRUCTOR
    ************************************ */

    /**
     * Constructs a new ClientHandler. Opens the Input and Output streams.
     *
     * @param sock The client socket
     * @param server  The connected server
     */
    public ClientHandler(Socket sock, Server server) throws IOException{
        try {
            in = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            out = new BufferedWriter(
                    new OutputStreamWriter(sock.getOutputStream()));
            this.sock = sock;
            this.server = server;
        } catch (IOException e) {
            this.shutdown();
        }
    }

    /* ************************************
               SETTERS AND GETTERS
    ************************************ */

    /**
     * Provides the name of the client specified in {@link Protocol#ANNOUNCE}
     * @ensures name != null && name obeys {@link Protocol} rules
     * @return name of Client
     */
    public String getName() {
        return this.name;
    }

    public boolean isSupportsLobby() {
        return this.supportsLobby;
    }

    public boolean isSupportsChat() {
        return supportsChat;
    }

    /**
     * Splits the argument according to {@link Protocol#DELIMITER}
     * @requires msg.contains({@link Protocol#DELIMITER})
     * @ensures Array of split strings
     */
    public String[] getArgSplit(String msg) {
        return msg.split(Pattern.quote(Protocol.DELIMITER));
    }

    public Object getInsertExplodeLock() {
        return insertExplodeLock;
    }

    public boolean isAwaitFavorResponse() {
        return awaitFavorResponse;
    }

    public void setAwaitFavorResponse(boolean awaitFavorResponse) {
        this.awaitFavorResponse = awaitFavorResponse;
    }

    /* ************************************
                     NETWORK
    ************************************ */

    /**
     * Handles the Hello handshake. First, the Client sends an {@link Protocol#ANNOUNCE}
     * message and the client's name is set. Server replies with a {@link Protocol#WELCOME}
     * if the name obeys the rules set in the protocol. Client replies with a
     * {@link Protocol#REQUESTGAME}, and the server asks the client how many players the game
     * should have. The number will define how long the ServerSocket will
     * accept connections.
     */
    public synchronized void handleHello()
            throws RuntimeException, ClientUnavailableException, IOException{
        String clientAns;
        String[] splittedClientAns;
        String command;
        String clientName;

        // -------- ANNOUNCE --------
        clientAns = this.readMessage();
        splittedClientAns = this.getArgSplit(clientAns);

        // splittedClientAns[0] = "ANNOUNCE"; Taken for granted.
        try {
            command = splittedClientAns[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Client did not provide a command");
        }

        // splittedClientAns[1] is player's name.
        try {
            clientName = splittedClientAns[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            this.shutdown();
            throw new ArrayIndexOutOfBoundsException("Client did not provide a name");
        }

        // Checks for Chat and Lobby because my server can handle only that.
        for (String string : splittedClientAns) {
            if (string.equals("C"))
                supportsChat = true;
            if (string.equals("L"))
                supportsLobby = true;
            if (string.equals("S"))
                supportsCombos = true;
        }

        // Name checking.
        if (server.isNameValid(clientName)) {
            this.name = clientName;
            try {
                if (server.getClients().isEmpty()) {
                    this.sendMessage(Protocol.WELCOME + Protocol.DELIMITER +
                            WELCOME_MESSAGE + " You are the first one.");
                } else {
                    // Get names of already-existing players, separated by a comma
                    String existingClients = server.getClients().stream()
                            .map(ClientHandler::getName)
                            .collect(Collectors.joining(", "));
                    this.sendMessage(Protocol.WELCOME + Protocol.DELIMITER +
                            WELCOME_MESSAGE + " The game has been joined by " +
                            existingClients + ".");
                }
            } catch (ClientUnavailableException e) {
                throw new ClientUnavailableException
                        ("\"Could not send WELCOME command to client\"");
            }
        } else if (!server.isNameValid(clientName)) {
            this.sendMessage(Protocol.ERROR + Protocol.DELIMITER +
                    Protocol.INVALID_NAME);
        } else if (!server.isNameTaken(clientName)) {
            this.sendMessage(Protocol.ERROR + Protocol.DELIMITER +
                    Protocol.DUPLICATE_NAME);
        }

        // Client gets the WELCOME command and sends back REQUESTGAME
        // -------- REQUEST GAME --------

        clientAns = readMessage();
        splittedClientAns = this.getArgSplit(clientAns);

        // splittedClientAns[0] = "REQUESTGAME"; Taken for granted.
        try {
            command = splittedClientAns[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            this.shutdown();
            throw new ArrayIndexOutOfBoundsException("Client did not provide a command");
        }

        // My server only supports the normal game.
        boolean doesRequestContainNormalGame =
                Arrays.stream(splittedClientAns)
                        .anyMatch("N"::equals);

        if (!doesRequestContainNormalGame) {
            throw new ProtocolException("Request command does not contain a normal game");
        }
    }

    /**
     * Sends the argument over the socket-connection to the Client.
     * @param msg != null
     * @throws ClientUnavailableException if Client is unreachable.
     */
    public synchronized void sendMessage(String msg)
            throws ClientUnavailableException {
        if (out != null) {
            try {
                // Print the current stack trace for debugging

                System.out.println("> [" + name + "] Outgoing: " + msg);
//                System.out.println("Msg is in clienthandler: [" + msg + "]");
                out.write(msg);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                throw new ClientUnavailableException("Could not write "
                        + "to client.");
            }
        } else {
            throw new ClientUnavailableException("Could not write "
                    + "to client.");
        }
    }

    /**
     * Reads strings of the stream of the socket connection. The method is
     * used before the run() method is started. The method is used
     * to retrieve the Client's message from the socket connection in
     * cases in which the Server must expect a certain command. For example,
     * in {@link ClientHandler#handleHello()}, this method is used because,
     * at certain points, the {@link ClientHandler} expects either
     * {@link Protocol#ANNOUNCE} or {@link Protocol#REQUESTGAME}.
     *
     * @requires in != null
     * @return Client's message from the socket connection.
     * @ensures result != null
     * @throws ClientUnavailableException if answer is null
     * @throws ClientUnavailableException if the method could not read
     * from server.
     * @throws ClientUnavailableException if input socket is null
     */
    public String readMessage() throws ClientUnavailableException {
        if (in != null) {
            try {
                String answer = in.readLine();
                if (answer == null) {
                    throw new ClientUnavailableException("Could not read "
                            + "from server.");
                }
                return answer;
            } catch (IOException e) {
                throw new ClientUnavailableException("Could not read "
                        + "from server.");
            }
        } else {
            throw new ClientUnavailableException("Nothing to read "
                    + "from server.");
        }
    }

    public synchronized void handleLobbySize() throws ClientUnavailableException{
        String clientAns;
        String[] splittedClientAns;
        int numberOfPlayers = 0;

        try {
            this.sendMessage(Protocol.PRIVATE + Protocol.DELIMITER +
                    "How many players do you want the game to have? Type \"private\" <from 2 to 5>");
        } catch (ClientUnavailableException e) {
            throw new RuntimeException(e);
        }

        clientAns = readMessage();
        splittedClientAns = this.getArgSplit(clientAns);

        System.out.println("> [" + name + "] Incoming: " + clientAns);

        // splittedClientAns[0] = "PRIVATE"; Taken for granted.
        try {
            String command = splittedClientAns[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            this.shutdown();
            System.out.println("> Client did not provide a command.");
            System.exit(0);
        }

        try {
            numberOfPlayers = Integer.parseInt(splittedClientAns[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            this.shutdown();
            System.out.println("> Client did not provide a number.");
            System.exit(0);
        } catch (NumberFormatException e) {
            this.shutdown();
            throw new NumberFormatException("\n> Client's message is not a number.");
        }

        if (numberOfPlayers < 2 || numberOfPlayers > 5) {
            this.sendMessage("There must be between 2 and 5 players in each game");
            this.shutdown();
            System.exit(0);
        }

        server.setNumberOfPlayersToPlay(numberOfPlayers);

        try {
            this.sendMessage(Protocol.PRIVATE + Protocol.DELIMITER +
                    "The game will have " + numberOfPlayers + " players. " +
                    "You have been put in a lobby.");
        } catch (ClientUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Shut down the connection to this client by closing Input & Output
     * streams, as well as the socket. Prints a message to standard output
     * that this ClientHandler left.
     */
    public void shutdown() {
        isShuttingDown = true;
        System.out.println("> [" + this.name + "] disconnected.");
        server.setNextClientNo(-1);
        try {
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        server.setNumberOfCurrentPlayers(-1);
        server.removeClient(this);
    }

    /* ************************************
                    GENERAL
    ************************************ */


    /**
     * Continuously listens for client's input and forwards the input to the
     * {@link #handleCommand(String)} method.
     */
    @Override
    public void run() {
        String msg;
        try {
            msg = in.readLine();
            while (msg != null) {
                System.out.println("> [" + name + "] Incoming: " + msg);
                handleCommand(msg);
//                out.newLine();
//                out.flush();
                msg = in.readLine();
            }
        } catch (IOException e) {
            if (!isShuttingDown)
                try {
                    throw new ServerUnavailableException("Could not read from socket");
                } catch (ServerUnavailableException ex) {
                    throw new RuntimeException(ex);
                }
        }
    }


    /**
     * Returns the first object of the splitted message that
     * came from the socket.
     * <p>
     * For example, if the splitted message is
     * {@link Protocol#PLAYMOVE}|{@link network.model.Card#FUTURE}, then
     * this method returns {@link Protocol#PLAYMOVE}
     * @requires splittedMsg != null && !splittedMsg.isEmpty()
     * @throws ArrayIndexOutOfBoundsException if splittedMsg.size() = 0
     * @param splittedMsg != null
     * @ensures command of Protocol
     */
    public String getProtocolCmd(String[] splittedMsg)
            throws ArrayIndexOutOfBoundsException{
        String protocolCmd;
        try {
            protocolCmd = splittedMsg[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            this.shutdown();
            throw new ArrayIndexOutOfBoundsException("Client did not provide a command");
        }
        return protocolCmd;
    }

    /**
     * Handles commands received from the client by calling the according
     * methods in {@link Server}. This method is called whenever the Client
     * sends a string over the socket.
     *
     * @param socketMsg command from client
     * @throws IOException if an IO errors occur.
     * @throws ArrayIndexOutOfBoundsException if client did not
     * provide a command.
     * @throws ProtocolException if received command is invalid.
     */
    private void handleCommand(String socketMsg)
            throws IOException, ArrayIndexOutOfBoundsException, ProtocolException {
        dummyCounter++;
        String[] clientMsgSplitted = this.getArgSplit(socketMsg);
        String protocolCmd = this.getProtocolCmd(clientMsgSplitted);

        //First player to play a move in the game
        if (dummyCounter == 1 && clientMsgSplitted[0].equals(Protocol.PLAYMOVE)) {
            timerActivated = true;
        }

        if (protocolCmd.equals(Protocol.ABORT) && server.getClients().size() == 1) {
            System.out.println("First player disconnected");
            System.exit(-1);
        }
        else if (protocolCmd.equals(Protocol.BROADCAST)) {
                server.doChatBroadcast(clientMsgSplitted[1], this);
        }
        else if ( (!server.isClientsTurn(this) && !awaitFavorResponse) && !timerActivated ) {
                try {
                    this.sendMessage(Protocol.PRIVATE + Protocol.DELIMITER +
                            "It is not your turn.");
                } catch (ClientUnavailableException e) {
                    throw new RuntimeException(e);
                }
            }
        else {
                switch (protocolCmd) {
                    case Protocol.PLAYMOVE -> {
                        if (!timerActivated || dummyCounter == 1) {
                            nopeCardsCounter = 0;
                            resumeGameHasBeenCalled = 0;
                            onCardPlayedStartNope(clientMsgSplitted);
                        } else {
                            if ( clientMsgSplitted[1].contains("nope") || clientMsgSplitted[1].contains("NOPE"))
                                this.onNopeCardPlayed(clientMsgSplitted);
                        }
//                        this.normalFlowOfGame(clientMsgSplitted);
                    }
                    case Protocol.INSERTEXPLODE -> {
                        server.doInsertExplode(Integer.parseInt(clientMsgSplitted[1]),
                                this);
                        server.resumeGame();
                    }
                    case Protocol.PRIVATE -> {
                        server.doPrivate(clientMsgSplitted[1],
                                server.getClients().indexOf(this));
                        server.resumeGame();
                    }
                    case Protocol.ABORT -> {
                        server.removePlayerFromGame(this);
                        this.shutdown();
                        if (server.getClients().size() == 1) {
                            server.doBroadcast("Other client(s) disconnected. You won (by luck).");
                            System.exit(-1);
                        } else {
                            server.doBroadcast(this.name + " just left! His/her cards were lost.");
                        }
                        server.resumeGame();
                    }
                    default -> {
                        try {
                            this.sendMessage(Protocol.ERROR + Protocol.DELIMITER +
                                    Protocol.UNRECOGNIZED);
                        } catch (ClientUnavailableException e) {
                            throw new RuntimeException(e);
                        }
                        server.resumeGame();
                    }
                }
            }
        }

    public void onNopeCardPlayed(String[] clientMsgSplitted) {
        if (timerActivated) {
            nopeCardsCounter++;
            server.doBroadcast("A NOPE card was played! Counter: " + nopeCardsCounter);
            onCardPlayedStartNope(clientMsgSplitted); // Restart the timer
        }
    }

    public void onCardPlayedStartNope(String[] clientMsgSplitted) {
        // Keeping the initial move. Assumes it was not a NOPE
        if (!clientMsgSplitted[1].contains("nope")) {
            initialSequence = clientMsgSplitted;
        }

        if (timerActivated) {
            if (nopeTimer != null) {
                nopeTimer.cancel();
            }
            timerActivated = false;
        }

        nopeTimer = new Timer();
        resumeGameHasBeenCalled += 1;

        server.doBroadcast(this.name + " wanted to " + clientMsgSplitted[1]);
        server.doBroadcast("Starting a 7 seconds timer in which you can type <play nope>...");
        TimerTask nopeCountdown = new TimerTask() {

            @Override
            public void run() {
                // Execute move
                if (nopeCardsCounter % 2 == 0) {
                    normalFlowOfGame(initialSequence);
                    server.doBroadcast("Server executed the move.");
                } else {
                    server.doBroadcast("Server did not execute the move.");
                    server.removeCardFromPlayerHand(Arrays.copyOfRange(
                            initialSequence, 1, initialSequence.length));
                    server.resumeGame();
                }
                timerActivated = false;
            }
        };

        timerActivated = true;
        nopeTimer.schedule(nopeCountdown, 7000); // 7 seconds countdown
    }

    public void normalFlowOfGame(String[] clientMsgSplitted) {
        if (!awaitFavorResponse)
            server.doPlayMove(Arrays.copyOfRange(
                            clientMsgSplitted, 1, clientMsgSplitted.length),
                    this);
        else
            server.doFavorResponse(clientMsgSplitted[1], this);
        this.awaitFavorResponse = false;
        server.resumeGame();
    }
}
