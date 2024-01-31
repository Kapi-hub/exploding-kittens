package network.Protocol;

/**
 * network.Protocol.Protocol for the mentor group of Catalina, Ovidiu and Cosmin
 * 
 * This is the standard protocol the group will use in their game of EXPLODING KITTENS.
 * Whenever something changes, you will get a ping in our mentoring group channel.
 * If there are questions or remarks, please ask them in the corresponding thread of MS Teams.
 */
public class Protocol  {
	//--------------General Implementation--------------//

	/**
	 * the seperator used for indicating the command and different arguments
	 */
	public static final String DELIMITER = "|";

	/**
	 * enum containing all possible types of cards
	 */
	public enum	cardType{
		EXPLODE, DEFUSE, ATTACK, FAVOR, NOPE, SHUFFLE, SKIP, FUTURE, // Special Cards
		TACOCAT, CATTERMELLON, POTATO, BEARD, RAINBOW // Regular Cards
	}
	
	/**
	 * The maximum amount of charachters a name can be. the username should also not contain the common seperator
	 */
	public static final int MAX_USERNAME_CHARACTERS = 15;
	//--------------------------------------------------//
	
	//-----------------Joining A Server-----------------//
	/** The first command in the handshake. Announces player's intention to connect to server.*/
	public static final String ANNOUNCE = "ANNOUNCE";

	/** Server's response to ANNOUNCE. This welcomes the guest and tells everyone a new player has joined.*/
	public static final String WELCOME = "WELCOME";

	//--------------------------------------------------//
	
	//-----------------Starting A Game------------------//
	/** After the server welcoming the player, the player must request a game, which can be either
	 * Normal (N), Special(E) or a Team-play (T). */
	public static final String REQUESTGAME = "REQUESTGAME";

	/**	The message is sent by the server when all players indicated their game preference.
	 * A START message is send to all players, containing the cards in their hand, the
	 * starting top card and the player who starts */
	public static final String START = "START";

	//--------------------------------------------------//

	//-----------------Playing The Game-----------------//
	/** Sent by the server when the player wants to draw a card.*/
	public static final String DRAW = "DRAW";

	/** Sent by the server in response to the player's choice of playing a SEE THE FUTURE card.
	 * It should contain the next top 3 cards from the Discard Pile.*/
	public static final String SEETHEFUTURE = "SEETHEFUTURE";

	/**
	 * Sent by the server to all players.
	 * Contains the current player (whose turn is right now) and the new top card.
	 * This is not mandatory, but is good for a good flow of the game.
	 */
	public static final String NEXTTURN = "NEXTTURN";

	/**
	 * Sent by the server to the player who won, hence indicating he/she won.
	 * Because a player explodes (i.e. is out of the game, i.e. Socket is closed) at the end of the game,
	 * nobody will be left.
	 * That is why you don't have to broadcast it to everyone.
	 * */
	public static final String GAMEOVER = "GAMEOVER";

	/** Sent by the client when the client wants to make a move.
	 * Takes as arguments the card and the name of the player who makes the move.*/
	public static final String PLAYMOVE = "PLAYMOVE";

	/** Sent by the client when the client played a DEFUSE and has to insert back the EXPLODE card.
	 * Takes as arguments a number which must be between 1 and the length of the deck.
	 * Keep in mind that the server will automatically take the DEFUSE card from the player's hand
	 * 	and will just ask, through a simple string, where does the player want to put the card
	 * 	(at which the respective player must reply with a valid index).*/
	public static final String INSERTEXPLODE = "INSERTEXPLODE";

	/** Sent by the client when the client decides it wants to leave the game.
	 * That is, intentionally quits.
	 * If the problem is the connection, then the server must raise an error and handle the
	 * close of the socket carefully. */
	public static final String ABORT = "ABORT";

	/** Sent by the server when there is a broadcast (message to all players) to be made.
	 * Sent by the client when the client has the Chat implemented and wants to send
	 * a message to players.
	 *
	 * For example, when 4 players make a move one after another, the server should tell
	 * everybody which card was played. */
	public static final String BROADCAST = "BROADCAST";

	/** Sent by the server when there is a need of sending a message to a specific player.
	 * For example, when the player is out, the Server must tell him this (in an ordered manner,
	 * not simply closing the connection). */
	public static final String PRIVATE = "PRIVATE";
	//--------------------------------------------------//

	//----------------------Errors----------------------//
	/** When a player causes an error to occur in the server, this message in sent to the player who caused the error, and it should be handled correctly. */
	public static final String ERROR = "ERROR";

	/** Array of all possible errors that can occur when using the protocol.
	 * -> This is private since it should not be used outside of this class*/
	private static final String[] ERRORNAMES = {"DUPLICATE_NAME", "INVALID_MOVE", "INVALID_NAME", "UNRECOGNIZED", "LOBBY_FULL", "REQUEST_GAME_ERROR", "INVALID_INDEX", "EXIT_PROGRAM", "SERVER_UNAVAILABLE"};

	public static final String DUPLICATE_NAME = ERRORNAMES[0];
	public static final String INVALID_MOVE = ERRORNAMES[1];
	public static final String INVALID_NAME = ERRORNAMES[2];
	public static final String UNRECOGNIZED = ERRORNAMES[3];
	public static final String LOBBY_FULL = ERRORNAMES[4];
	public static final String REQUEST_GAME_ERROR = ERRORNAMES[5];
	public static final String INVALID_INDEX = ERRORNAMES[6];
	public static final String EXIT_PROGRAM = ERRORNAMES[7];
	public static final String SERVER_UNAVAILABLE = ERRORNAMES[8];
	//--------------------------------------------------//
}
