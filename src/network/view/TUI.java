package network.view;

import network.model.Card;
import network.server.Server;

import java.util.ArrayList;
import java.util.stream.Collectors;


/** Used for communicating with the client */
public class TUI {

    /** Used to send private messages & broadcasts */
    private Server server;

    public TUI(Server server) {
        this.server = server;
    }

    public void promptDelimiter() {
        System.out.print("\n|---------------------------------------------------|\n\n");
    }

    public void promptInput() {
        System.out.print("\n>> Input: ");
    }

    public void promptStartGameMenu() {
        System.out.println("[!] Welcome, this is a game of Exploding Kittens.");
        System.out.println("[!] If you want to play against a Computer, press" +
                " 1 and Enter.");
        System.out.println("[!] If you want to play two computers play against" +
                " each other, press 2 and Enter.");
        promptInput();
    }

    public void promptStartGameWithHumanAndComputers() {
        promptDelimiter();
        System.out.println("[!] You have decided that you would like to play against a computer.");
        System.out.println("[!] Specify the number of computer players you" +
                " would like to play with.");
        promptInput();
    }

    public void promptStartGameWithComputers() {
        promptDelimiter();
        System.out.println("[!] You have decided that you not like to play.");
        System.out.println("[!] You let the computer players play on their own.");
        System.out.println("[!] Specify the number of computer players that" +
                " will take part in the game.");
        promptInput();
    }

    public void promptConfirmationOfPlayersWhenHumanPlaying(int numberOfPlayers) {
        promptDelimiter();
        System.out.println("[!] You have decided to play with " + numberOfPlayers
        + " computer player(s).");
    }

    public void promptConfirmationOfPlayersWhenComputersPlaying(int numberOfPlayers) {
        promptDelimiter();
        System.out.println("[!] There will be " + numberOfPlayers + " computer player(s).");
        promptDelimiter();
    }

    public void promptPlayerHand(String playerName,
                                 ArrayList<Card> playerHand,
                                 int currentPlayerIndex) {
        server.doPrivate("Your hand: " + playerHand, currentPlayerIndex);
    }

    public void promptInsertBackExplode(int lastDeckCardIndex) {
        System.out.println("\n[!] " + "You drew an EXPLODE card.");
        System.out.println("[!] " + "Write an index from 0 to " +
                lastDeckCardIndex + " (including) to insert back the EXPLODE card");
    }

    public void promptExplodeDefuse(String playerName) {
        System.out.println("[!] " + playerName + " drew an EXPLODE card, but he/she had a DEFUSE.");
        System.out.println("[!] " + playerName + " inserted into the deck the EXPLODE card wherever he/she preferred.");
    }

    public void informWinner(String name) {
        System.out.println(name + " won!");
    }

    public void promptKickExplodeNoDefuse(String name) {
        System.out.println('\n' + name + " has been kicked since the player " +
                "drew an EXPLODE and did not have any DEFUSE card.");
    }

    public void raiseWarningWrongInput(String illegalMove) {
        System.out.println("\n[!] Illegal move: " + illegalMove);
    }

    public void informFirstMove(int currentPlayerIndex) {
        server.doPrivate("You make the first move!", currentPlayerIndex);
    }

    public void raiseWarningAtExplodePlay() {
        System.out.println("[!] " + "You cannot play an EXPLODE card. You can only draw one " +
                "from the deck.");
    }

    public void raiseWarningAtDefusePlay() {
        System.out.println("[!] You cannot play a DEFUSE card.");
        System.out.println("[!] It is drawn from your hand upon drawing an EXPLODE.");
    }

    public void raiseWarningAtDrawIllegalArgs() {
        System.out.println("[!] Syntax: draw");
    }

    public void promptShuffleConfirmation() {
        System.out.println("\n[!] The deck has been successfully shuffled.");
    }


    public void promptLastThreeCardsFromDeck(
            Card lastCard,
            Card secondToLastCard,
            Card thirdToLastCard
    ) {
        System.out.println("\n[!] Last card:           " + lastCard);
        System.out.println("[!] Second to last card: " + secondToLastCard);
        System.out.println("[!] Third to last card:  " + thirdToLastCard);
    }

    public void raiseWarningHasTurnsToStay(int turnsToStay) {
        promptLeftTurnsToStay(turnsToStay);
        System.out.println("[!] A turn is ended when you draw a card or play a SKIP.");
    }

    public void promptLeftTurnsToStay(int turnsToStay) {
        System.out.println("\n[!] You have " + turnsToStay + " left turn(s) to take.");
    }

    public void informPlayersCurrentPlayerHasToPlay(String playerNameToPlay,
                                                    int turnsToPlay) {
        server.doBroadcast(playerNameToPlay + " has " + turnsToPlay
                + " turns to take.");
    }

    public void raiseWarningFavorNoThirdArg() {
        System.out.println("[!] When playing a FAVOR, you must specify the name of the player.");
    }

    public void raiseWarningFavorPlayerNotFound() {
        System.out.println("[!] Player not found.");
    }

    public void informGiveRandomCardUponFavor() {
        System.out.println("[!] The player whose turn is now played a FAVOR. ");
        System.out.println("[!] Type \"play <card name that you want to give>.\" ");
    }
}
