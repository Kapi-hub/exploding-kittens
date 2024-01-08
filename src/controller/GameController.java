package controller;

import model.*;
import view.TUI;

import java.util.ArrayList;
import java.util.Scanner;

public class GameController {
    private static final int HUMAN_PLAYING = 1;
    private static final int COMPUTERS_PLAYING = 2;

    private static Scanner userInput = new Scanner(System.in);

    private static boolean continueGame = true;
    private static int decisionOfHowToPlay;
    private static int numberOfComputerPlayers;
    private static boolean gameHasWinner;
    private static String move;

    private static Game game;
    private static ArrayList<Player> players;
    private static ArrayList<String> commands;
    private static Player currentPlayer;


    public static void startGame() {
        players = new ArrayList<>();
        TUI.promptStartGameMenu();
        decisionOfHowToPlay = Integer.parseInt(userInput.nextLine());
        switch (decisionOfHowToPlay) {
            case HUMAN_PLAYING:
                TUI.promptStartGameWithHumanAndComputers();
                numberOfComputerPlayers = Integer.parseInt(userInput.nextLine());
                TUI.promptConfirmationOfPlayersWhenHumanPlaying
                        (numberOfComputerPlayers);
                players.add(new HumanPlayer("Alex"));
                break;
            case COMPUTERS_PLAYING:
                TUI.promptStartGameWithComputers();
                numberOfComputerPlayers = Integer.parseInt(userInput.nextLine());
                TUI.promptConfirmationOfPlayersWhenComputersPlaying
                        (numberOfComputerPlayers);
                break;
        }
    }

    public static void initialiseGame() {
        // The player "You" is you, the one reading this.
        for (int index = 0; index < numberOfComputerPlayers; index++) {
            players.add(new ComputerPlayer("BOT " + index));
        }
        game = new Game(players);
        game.setUp();
    }

    public static void continueGame() {
        gameHasWinner = false;
        move = "";
        while (!gameHasWinner) {
            TUI.promptDeck(
                    game.getDeckObject().getDeckArray().size());
            TUI.promptDiscardPile(game.getDiscardPile());
            game.getPlayers().forEach(player -> TUI.promptPlayerHand(
                    player.getName(),
                    player.getHand()));
            TUI.promptDelimiter();
            currentPlayer = game.getPlayers().get(game.getCurrentPlayerTurnIndex());
            askAndSendInputForProcessing();

            game.incrementPlayerTurnIndex();
        }
    }

    public static void askAndSendInputForProcessing() {
        if (currentPlayer instanceof HumanPlayer) {
            move = askForStringInput();
            game.processMove(move);
        } else {
            move = ((ComputerPlayer) currentPlayer).doMove(game);
            game.processMove(move);
        }
    }

    public static int askIndexOfReinsertingExplode() {
        int desiredIndex;
        int lastDeckCardIndex;
        if (currentPlayer instanceof HumanPlayer) {
            lastDeckCardIndex = game.getDeckObject().getDeckArray().size() - 1;
            TUI.promptInsertBackExplode(lastDeckCardIndex);
            desiredIndex = askForIntInput();
            TUI.promptDelimiter();
            return desiredIndex;
        } else {
            return (game.getDeckObject().getDeckArray().size() - 1);
        }
    }

    public static void informPlayerKick() {
        TUI.promptKickExplodeNoDefuse(currentPlayer.getName());
    }

    public static void informWinner() {
        TUI.informWinner(game.getPlayers().get(0).getName());
    }

    public static int askForIntInput() {
        TUI.promptInput();
        return Integer.parseInt(userInput.nextLine());
    }

    public static String askForStringInput() {
            TUI.promptInput();
            return userInput.nextLine();
    }

    public static void raiseWarningWrongInput(String illegalMove) {
        TUI.raiseWarningWrongInput(illegalMove);
        askAndSendInputForProcessing();
    }

    public static void main(String[] args) {
        startGame();
        initialiseGame();
        continueGame();
    }
}
