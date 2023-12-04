package controller;

import view.TUI;

import java.util.Scanner;

public class GameController {
    private static final int HUMAN_PLAYING = 1;
    private static final int COMPUTERS_PLAYING = 2;
    private static boolean continueGame = true;

    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in);
        int decisionOfHowToPlay;
        int numberOfJustComputerPlayers;
        int numberOfComputerPlayersWithHuman;

        do {
            TUI.promptStartGameMenu();
            decisionOfHowToPlay = userInput.nextInt();
            switch(decisionOfHowToPlay) {
                case HUMAN_PLAYING:
                    TUI.promptStartGameWithHumanAndComputers();
                    numberOfComputerPlayersWithHuman = userInput.nextInt();
                    TUI.promptConfirmationOfPlayersWhenHumanPlaying
                            (numberOfComputerPlayersWithHuman);
                    userInput.next();
                    break;
                case COMPUTERS_PLAYING:
                    TUI.promptStartGameWithComputers();
                    numberOfJustComputerPlayers = userInput.nextInt();
                    TUI.promptConfirmationOfPlayersWhenComputersPlaying
                            (numberOfJustComputerPlayers);
                    userInput.next();
                    break;
            }
            continueGame = false;
        } while(continueGame);


    }
}
