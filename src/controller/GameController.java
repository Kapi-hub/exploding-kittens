package controller;

import model.ComputerPlayer;
import model.Game;
import model.HumanPlayer;
import model.Player;
import view.TUI;

import java.util.ArrayList;
import java.util.Arrays;
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


    public static void startGame() {
        players = new ArrayList<>();
        TUI.promptStartGameMenu();
        decisionOfHowToPlay = userInput.nextInt();
        switch (decisionOfHowToPlay) {
            case HUMAN_PLAYING:
                TUI.promptStartGameWithHumanAndComputers();
                numberOfComputerPlayers = userInput.nextInt();
                TUI.promptConfirmationOfPlayersWhenHumanPlaying
                        (numberOfComputerPlayers);
                players.add(new HumanPlayer("You"));
                break;
            case COMPUTERS_PLAYING:
                TUI.promptStartGameWithComputers();
                numberOfComputerPlayers = userInput.nextInt();
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

    public static void doMove(String move) {
        commands = new ArrayList<>(Arrays.asList(move.split(" ")));
        String playOrDraw = commands.get(0).toLowerCase();
        switch (playOrDraw) {
            case "play":

                break;
            case "draw":
                break;
        }
    }

    public static void continueGame() {
        gameHasWinner = false;
        move = "";
        do {
            TUI.promptDeck(
                    game.getDeckObject().getDeckAsArrayList().size());
            TUI.promptDiscardPile(game.getDiscardPile());
            game.getPlayers().forEach(player -> TUI.promptPlayerHand(
                            player.getName(),
                            player.getHand()));
            TUI.promptInput();
            move = userInput.nextLine();
            doMove(move);
        } while(!gameHasWinner);

    }

    public static void main(String[] args) {
        startGame();
        initialiseGame();
        continueGame();
    }
}
