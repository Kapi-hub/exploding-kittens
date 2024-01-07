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
        + " computer players.");
        System.out.println("[!] Press any key to continue.");
        promptDelimiter();
    }

    public static void promptConfirmationOfPlayersWhenComputersPlaying(int numberOfPlayers) {
        promptDelimiter();
        System.out.println("[!] There will be " + numberOfPlayers + " computer player(s).");
        System.out.println("[!] Press any key to continue.");
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


}
