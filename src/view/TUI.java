package view;

import java.sql.SQLOutput;

public class TUI {
    public static void promptStartGameMenu() {
        System.out.println("[!] Welcome, this is a game of Exploding Kittens.");
        System.out.println("[!] If you want to play against a Computer, press" +
                " 1 on your keyboard.");
        System.out.println("[!] If you want to play two computers play against" +
                " each-other, press 2 on your keyboard.");
    }

    public static void promptStartGameWithHumanAndComputers() {
        System.out.println("[!] You have decided that you would like to play.");
        System.out.println("[!] Specify the number of computer players you" +
                " would like to play with.");
    }

    public static void promptStartGameWithComputers() {
        System.out.println("[!] You have decided that you not like to play.");
        System.out.println("[!] You let the computer players play on their own.");
        System.out.println("[!] Specify the number of computer players that" +
                " will take part in the game.");
    }

    public static void promptConfirmationOfPlayersWhenHumanPlaying(int numberOfPlayers) {
        System.out.println("[!] You have decided to play with " + numberOfPlayers
        + " computer players.");
        System.out.println("[<<<<<<<< PRESS ANY KEY TO CONTINUE >>>>>>>>].");
    }

    public static void promptConfirmationOfPlayersWhenComputersPlaying(int numberOfPlayers) {
        System.out.println("[!] There will be " + numberOfPlayers + " computer players.");
        System.out.println("[<<<<<<<< PRESS ANY KEY TO CONTINUE >>>>>>>>].");
    }




}
