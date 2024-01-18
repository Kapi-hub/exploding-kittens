package view;

import model.Card;
import model.Player;

import java.util.ArrayList;

public class TUI {

    public static void promptDelimiter() {
        System.out.print("\n|---------------------------------------------------|\n\n");
    }

    public static void promptInput() {
        System.out.print("\n>> Input: ");
    }

    public static void promptStartGameMenu() {
        System.out.println("[!] Welcome, this is a game of Exploding Kittens.");
        System.out.println("[!] If you want to play against a Computer, press" +
                " 1 and Enter.");
        System.out.println("[!] If you want to play two computers play against" +
                " each other, press 2 and Enter.");
        promptInput();
    }

    public static void promptStartGameWithHumanAndComputers() {
        promptDelimiter();
        System.out.println("[!] You have decided that you would like to play against a computer.");
        System.out.println("[!] Specify the number of computer players you" +
                " would like to play with.");
        promptInput();
    }

    public static void promptStartGameWithComputers() {
        promptDelimiter();
        System.out.println("[!] You have decided that you not like to play.");
        System.out.println("[!] You let the computer players play on their own.");
        System.out.println("[!] Specify the number of computer players that" +
                " will take part in the game.");
        promptInput();
    }

    public static void promptConfirmationOfPlayersWhenHumanPlaying(int numberOfPlayers) {
        promptDelimiter();
        System.out.println("[!] You have decided to play with " + numberOfPlayers
        + " computer player(s).");
    }

    public static void promptConfirmationOfPlayersWhenComputersPlaying(int numberOfPlayers) {
        promptDelimiter();
        System.out.println("[!] There will be " + numberOfPlayers + " computer player(s).");
        promptDelimiter();
    }

    public static void promptDeck(int numberOfCardsInDeck) {
        System.out.print("\nDeck pile    : " + numberOfCardsInDeck + " cards remaining\n");
    }

    public static void promptDiscardPile(ArrayList<Card> discardPile) {
        System.out.print("Discard pile : ");
        discardPile.forEach(card -> System.out.print("| " +card.toString() + " | "));
        System.out.print("\n\n");
    }

    public static void promptPlayerHand(String playerName, ArrayList<Card> playerHand) {
        System.out.println(String.format("%-6s: %s", playerName, playerHand));
    }

    public static void promptInsertBackExplode(int lastDeckCardIndex) {
        System.out.println("\n[!] " + "You drew an EXPLODE card.");
        System.out.println("[!] " + "Write an index from 0 to " +
                lastDeckCardIndex + " (including) to insert back the EXPLODE card");
    }

    public static void promptExplodeDefuse(String playerName) {
        System.out.println("[!] " + playerName + " drew an EXPLODE card, but he/she had a DEFUSE.");
        System.out.println("[!] " + playerName + " inserted into the deck the EXPLODE card wherever he/she preferred.");
    }

    public static void informWinner(String name) {
        System.out.println(name + " won!");
    }

    public static void promptKickExplodeNoDefuse(String name) {
        System.out.println('\n' + name + " has been kicked since the player " +
                "drew an EXPLODE and did not have any DEFUSE card.");
    }

    public static void raiseWarningWrongInput(String illegalMove) {
        System.out.println("\n[!] Illegal move: " + illegalMove);
    }

    public static void promptFirstMovePlayer(ArrayList<Player> players, int playerIndexToPlayFirstMove) {
        System.out.println("[!] " + players.get(playerIndexToPlayFirstMove) + " makes the first move.");
        promptDelimiter();
    }

    public static void raiseWarningAtExplodePlay() {
        System.out.println("[!] " + "You cannot play an EXPLODE card. You can only draw one " +
                "from the deck.");
    }

    public static void raiseWarningAtDefusePlay() {
        System.out.println("[!] You cannot play a DEFUSE card.");
        System.out.println("[!] It is drawn from your hand upon drawing an EXPLODE.");
    }

    public static void raiseWarningAtDrawIllegalArgs() {
        System.out.println("[!] Syntax: draw");
    }

    public static void promptShuffleConfirmation() {
        System.out.println("\n[!] The deck has been successfully shuffled.");
    }

    public static void raiseWarningHasTurnsToStay(int turnsToStay) {
        System.out.println("[!] You cannot play.");
        System.out.println("[!] You have " + turnsToStay + " turns to stay.");
    }

    public static void promptLastThreeCardsFromDeck(
            Card lastCard,
            Card secondToLastCard,
            Card thirdToLastCard
    ) {
        System.out.println("\n[!] Last card:           " + lastCard);
        System.out.println("[!] Second to last card: " + secondToLastCard);
        System.out.println("[!] Third to last card:  " + thirdToLastCard);
    }
}
