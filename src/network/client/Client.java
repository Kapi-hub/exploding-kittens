package network.client;

import network.Protocol.Protocol;
import network.exceptions.ExitProgram;
import network.exceptions.InvalidClientMove;
import network.exceptions.ServerUnavailableException;
import network.model.Card;
import network.model.ComputerPlayer;
import network.model.Game;

import java.io.*;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class Client implements Runnable{
    private Socket sock;
    private InetAddress addr;
    private int port;
    private BufferedReader in;
    private BufferedWriter out;
    private Game game;
    private ClientTUI clientTUI;
    private String name;
    private boolean isShuttingDownByForce = false;
    private boolean isShuttingDownByWill = false;

    private boolean isComputerPlayer;
    private ComputerPlayer computerPlayer;

    private final Object lock = new Object();

    private static final String USAGE
            = "usage: <name> <address> <port> <true/false>";

    /* ************************************
           FEATURES & GAMES SUPPORTED
    ************************************ */

    private final static String FEATURE_CHAT = "C";
    private final static String FEATURE_COMBOS = "S";
    private final static String FEATURE_LOBBY = "L";
    private final static String NORMAL_GAME = "N";

    /* ************************************
                CONSTRUCTORS
    ************************************ */

    public Client(String[] args) {
        clientTUI = new ClientTUI(this);

        if (args.length != 4) {
            System.out.println(USAGE);
            System.exit(0);
        }

        addr = null;
        port = 0;
        sock = null;

        // check args[0] - the Client name
        try {
            name = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            clientTUI.printMessage(USAGE);
            clientTUI.printMessage("ERROR: name" + args[0] + " not defined");
            System.exit(0);
        }

        // check args[1] - the IP-address
        try {
            addr = InetAddress.getByName(args[1]);
        } catch (UnknownHostException e) {
            clientTUI.printMessage(USAGE);
            clientTUI.printMessage("ERROR: host " + args[1] + " unknown");
            System.exit(0);
        }

        // parse args[2] - the port
        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            clientTUI.printMessage(USAGE);
            clientTUI.printMessage("ERROR: port " + args[2]
                    + " is not an integer");
            System.exit(0);
        }

        // parse args[3] - true/false <=> isComputerPlayer/!isComputerPlayer
        try {
            this.isComputerPlayer = Boolean.parseBoolean(args[3]);
        } catch (NumberFormatException e) {
            clientTUI.printMessage(USAGE);
            clientTUI.printMessage("ERROR: boolean " + args[3]
                    + " is not actually a boolean");
            System.exit(0);
        }

        if (this.isComputerPlayer)
            computerPlayer = new ComputerPlayer(args);

        // tries to create a connection
        try {
            this.createConnection();
        } catch (ExitProgram e) {
            throw new RuntimeException(e);
        }
    }

    /* ************************************
               GETTERS AND SETTERS
    ************************************ */

    public Object getLock() {
        return this.lock;
    }

    public ClientTUI getClientTUI() {
        return clientTUI;
    }

    /* ************************************
                    NETWORK
    ************************************ */

    public void createConnection() throws ExitProgram {
        clearConnection();
        try {
            clientTUI.printMessage("Attempting to connect to " + addr + ":"
                    + port + ".");
            sock = new Socket(addr, port);
            in = new BufferedReader(new InputStreamReader(
                    sock.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(
                    sock.getOutputStream()));
        } catch (IOException e) {
            clientTUI.printMessage("ERROR: could not create a socket on "
                    + addr + " and port " + port + ".");
            System.exit(-1);
        }
            clientTUI.printMessage("Connection to " + addr + " and port " +
                    port + " initialised.\n");
    }

    public void clearConnection() {
        sock = null;
        in = null;
        out = null;
    }

    public synchronized void sendMessage(String msg)
            throws ServerUnavailableException {
        if (out != null) {
            try {
//                System.out.println("OUTGOING: [" + msg + "]");
                out.write(msg);
                out.newLine();
                out.flush();
            } catch (IOException e) {
                clientTUI.printMessage(e.getMessage());
                throw new ServerUnavailableException("Could not write "
                        + "to server.");
            }
        } else {
            throw new ServerUnavailableException("Could not write "
                    + "to server.");
        }
    }

    public String readLineFromServer() throws ServerUnavailableException {
        if (in != null) {
            try {
                String answer = in.readLine();
                if (answer == null) {
                    throw new ServerUnavailableException("Could not read "
                            + "from server.");
                }
                return answer;
            } catch (IOException e) {
                throw new ServerUnavailableException("Could not read "
                        + "from server.");
            }
        } else {
            throw new ServerUnavailableException("Connection is not " +
                    "initialised.");
        }
    }

    public void shutdown() {
        clientTUI.printMessage("Closing the connection.");
        try {
            in.close();
            out.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles commands received from the server by calling the according
     * methods.
     *
     * If the received input is invalid, send an "Unknown Command"
     * message to the server.
     *
     * @param msg command from client
     * @throws IOException if an IO errors occur.
     */
    private void handleCommandFromServer(String msg) throws IOException {
        String[] splittedMessage = this.getSpilt(msg);
        String protocolCommand = this.getCommandFromSplittedMessage(splittedMessage);
        String message;

        switch (protocolCommand) {
            case Protocol.DRAW:
                message = this.getStringFromSplittedMessage(splittedMessage);
                clientTUI.printMessageFromServer(message);
                clientTUI.printMessage("Type >> ");
                break;
            case Protocol.SEETHEFUTURE:
                clientTUI.printMessageFromServer(splittedMessage[1] + " | " +
                        splittedMessage[2] + " | " +
                        splittedMessage[3]);
                break;
            case Protocol.BROADCAST, Protocol.PRIVATE:
                message = this.getStringFromSplittedMessage(splittedMessage);
                clientTUI.printMessageFromServer(message);
                if (message.startsWith("Your hand: ")
                        || message.equals("It is not your turn.")
                        || message.equals("How many players do you want the game to have? "
                        + "Type \"private\" <from 2 to 5>")
                        || message.endsWith("in which you can play NOPEs..."))
                    clientTUI.printMessage("Type >> ");

                if (this.isComputerPlayer) {
                    // Understand hand and transform to String[]
                    if (message.startsWith("Your hand:")) {
                        decomposeComputerPlayerHand(Arrays.toString(Arrays.copyOfRange(splittedMessage,
                                1, splittedMessage.length)));
                    }
                    if (message.contains("has played a FAVOR to you.")) {
                        getCardInFavorResponse();
                    }

                    // if yes, bot needs to take a turn. If not, not his turn.
                    computerPlayer.setMyTurnOrNot(splittedMessage[1].contains("You make the first move!")
                            || splittedMessage[1].contains("It's your turn now!")
                            || splittedMessage[1].contains("It's still your turn"));
                    computerPlayer.setMustInsertBack(message.endsWith("to insert back the EXPLODE card"));
                    try {
                        insertExplodeComputerPlayer();
                    } catch (ExitProgram | ServerUnavailableException | InvalidClientMove e) {
                        throw new RuntimeException(e);
                    }
                    // Play a move after 1 second, such that the computer player receives
                    // his hand.
                    if (computerPlayer.isMyTurnOrNot()) {
                        {
//                            System.out.println("It's my turn!");
                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                        getMoveFromComputerPlayer(Arrays.copyOfRange(splittedMessage,
                                                1, splittedMessage.length));
                                    if (computerPlayer.isMustInsertBack()) {
                                        getIndexFromComputerPlayer();
                                    }
                                }
                            };
                            new Timer().schedule(timerTask, 1000);
                        }
                    }
                }
                break;
            default:
                System.out.println(msg);
                System.out.println("Unknown Command");
                break;
        }
    }

    public String getCommandFromSplittedMessage(String[] message) {
        String result;
        try {
            result = message[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Server did not provide a command");
        }
        return result;
    }

    public String getStringFromSplittedMessage(String[] message)
            throws ArrayIndexOutOfBoundsException {
        String result;
        try {
            result = message[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Server did not provide a message");
        }
        return result;
    }

    public void handleHello()
            throws ServerUnavailableException, ProtocolException {
        String serverReply;
        String[] spServerReply;

        // Client announces himself via ANNOUNCE
        this.sendMessage(Protocol.ANNOUNCE + Protocol.DELIMITER +
                this.name + Protocol.DELIMITER + FEATURE_CHAT +
                Protocol.DELIMITER + FEATURE_LOBBY + Protocol.DELIMITER +
        FEATURE_COMBOS);

        // Server sends a reply, either WELCOME or an ERROR
        serverReply = this.readLineFromServer();
        spServerReply = getSpilt(serverReply);

        if (spServerReply[0].equals(Protocol.ERROR)) {
            clientTUI.printMessageFromServer("Error. Invalid name or name already taken.");
            this.shutdown();
        } else if (spServerReply[0].equals(Protocol.WELCOME))
            clientTUI.printMessageFromServer(spServerReply[1]);

        this.sendMessage(Protocol.REQUESTGAME + Protocol.DELIMITER +
                NORMAL_GAME);
    }

    private void getIndexFromComputerPlayer() {
        try {
            this.doInsert(computerPlayer.getIndexToReinsertExplode());
        } catch (ExitProgram | ServerUnavailableException | InvalidClientMove e) {
            throw new RuntimeException(e);
        }
    }

    public void insertExplodeComputerPlayer() throws ExitProgram, ServerUnavailableException, InvalidClientMove {
        this.doInsert(computerPlayer.getIndexToReinsertExplode());
    }

    public void getMoveFromComputerPlayer(String[] splittedMessage) {
        try {
            this.doPlay(computerPlayer.doMove());
        } catch (ExitProgram | ServerUnavailableException | InvalidClientMove e) {
            throw new RuntimeException(e);
        }
    }

    public void decomposeComputerPlayerHand(String splittedMessage) {
        computerPlayer.decomposeHand(splittedMessage);
    }

    public void getCardInFavorResponse() {
        this.doPlay(computerPlayer.getCardInFavorResponse());
    }

    /* ************************************
                    METHODS
    ************************************ */

    public void init() {

        try {
            this.handleHello();
        } catch (ServerUnavailableException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        Thread streamInputHandler = new Thread(this);
        streamInputHandler.start(); // From Socket to console

        try {
            clientTUI.start();
        } catch (ServerUnavailableException | InvalidClientMove e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        String incomingMessage;

        try {
            while((incomingMessage = this.readLineFromServer()) != null) {
                this.handleCommandFromServer(incomingMessage);
            }
        } catch (IOException | ServerUnavailableException e) {
            if(!isShuttingDownByForce) {
                clientTUI.printMessageFromServer("closed the connection!");
                System.exit(0);
                this.shutdown();
                throw new RuntimeException(e);
            }
        }
    }

    public String[] getSpilt(String serverReply) {
        return serverReply.split(Pattern.quote(Protocol.DELIMITER));
    }

    public void sendFavorCard(String favorCard, String targetPlayerName) {
        try {
            // split[1] = "FAVOR"; split[2] targetName
            this.doPlay(favorCard.toUpperCase(), targetPlayerName);
        } catch (ArrayIndexOutOfBoundsException e) {
            clientTUI.printMessage("No name specified. Try again.");
            clientTUI.printMessage("Type >> ");
        }
    }

    public boolean isCatCard(String supposedCatCard) {
        return (supposedCatCard.equalsIgnoreCase(Card.CATTERMELLON.name()) ||
                supposedCatCard.equalsIgnoreCase(Card.BEARD.name()) ||
                supposedCatCard.equalsIgnoreCase(Card.RAINBOW.name()) ||
                supposedCatCard.equalsIgnoreCase(Card.POTATO.name()) ||
                supposedCatCard.equalsIgnoreCase(Card.TACOCAT.name()));
    }

    public void onTerminalShutdown() throws RuntimeException {
        clientTUI.printMessage("JVM shutdown triggered!");
        isShuttingDownByForce = true;
        if (!isShuttingDownByWill) {
            try {
                this.doAbort();
            } catch (ServerUnavailableException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
    /* ************************************
                      DO's
    ************************************ */

    public void doPlay(String... cmd ) {
        StringBuilder socketMsg = new StringBuilder().append(Protocol.PLAYMOVE);

        for (String cmdPart : cmd) {
            socketMsg.append(Protocol.DELIMITER).append(cmdPart);
        }

        try {
            this.sendMessage(socketMsg.toString());
        } catch (ServerUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void doInsert(String index) {
        try {
            this.sendMessage(Protocol.INSERTEXPLODE + Protocol.DELIMITER +
                    index);
        } catch (ServerUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void doDraw() {
        try {
            this.sendMessage(Protocol.PLAYMOVE + Protocol.DELIMITER +
                    "DRAW");
        } catch (ServerUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void doPrivateMessage(String message) {
        try {
            this.sendMessage(Protocol.PRIVATE + Protocol.DELIMITER +
                    message);
        } catch (ServerUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void doBroadcast(String message) {
        try {
            this.sendMessage(Protocol.BROADCAST + Protocol.DELIMITER +
                    message);
        } catch (ServerUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void doAbort() throws ServerUnavailableException {
        this.isShuttingDownByWill = true;
        this.sendMessage(Protocol.ABORT);
        this.shutdown();
    }

    /* ************************************
                      MAIN
    ************************************ */
    public static void main(String[] args) {
        System.out.println("Client started.");

        Client client = new Client(args);
        Runtime.getRuntime().addShutdownHook(new Thread(client::onTerminalShutdown));
        client.init();
    }
}
