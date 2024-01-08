package view;

import model.Card;

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
        promptDelimiter();
    }

    public static void promptConfirmationOfPlayersWhenComputersPlaying(int numberOfPlayers) {
        promptDelimiter();
        System.out.println("[!] There will be " + numberOfPlayers + " computer player(s).");
        promptDelimiter();
    }

    public static void promptDeck(int numberOfCardsInDeck) {
        System.out.print("Deck pile    : " + numberOfCardsInDeck + " cards remaining\n");
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
        System.out.println("Illegal move: " + illegalMove);
    }

}
