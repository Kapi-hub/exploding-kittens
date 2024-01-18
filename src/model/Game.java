package model;

import controller.GameController;
import view.TUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Game {
    private Deck deck;
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Card> discardPile;
    private int currentPlayerIndex;
    private Player currentPlayer;

    private final int NUMBER_OF_PLAYERS;

    /* ************************************
              GETTERS & SETTERS
    ************************************ */
    public Deck getDeckObject() {
        return deck;
    }

    public int getCurrentPlayerTurnIndex() {
        return currentPlayerIndex;
    }

    public void incrementPlayerTurnIndex() {
        int copy = currentPlayerIndex + 2;
        if (copy <= NUMBER_OF_PLAYERS)
            currentPlayerIndex++; // 1
        else {
            currentPlayerIndex = 0;
        }
        currentPlayer = players.get(currentPlayerIndex);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getNUMBER_OF_PLAYERS() { return NUMBER_OF_PLAYERS; }

    public ArrayList<Card> getDiscardPile() { return discardPile; }

    /* ************************************
                CONSTRUCTORS
    ************************************ */

    /**
     * @requires players != null;
     * @param players ArrayList of players of the game
     */
    public Game(ArrayList<Player> players) {
        players.forEach(player -> this.players.add(player));
        NUMBER_OF_PLAYERS =  this.players.size();
        currentPlayerIndex = (int) (Math.random() * NUMBER_OF_PLAYERS);
        currentPlayer  = players.get(currentPlayerIndex);
        deck = new Deck();
        discardPile = new ArrayList<>();
    }

    /* ************************************
                    METHODS
    ************************************ */

    /* ****** SET UP OF THE GAME ******* */

    /**
     *  Sets up the method exactly as the rules indicate.
     *      First method combines STEP 1 & 2 from the rules, removing the Exploding Kittens (4)
     *          and Defuses (6) cards.
     *      Second method deals the right number of cards to players. That is, each player gets
     *          a Defuse card. Then, depending on NUMBER_OF_PLAYERS, there are inserted 1 or 2
     *          Defuse cards back in the deck. Then, each player gets 7 cards from the deck, for
     *          a total of 8.
     *      Third method inserts NUMBER_OF_PLAYERS - 1 (such that there remains a winner) Exploding
     *          cards back into the deck. The deck is then shuffled and ready to be played.
     * @ensures Player's hands (player.getHand()) have 8 cards (7 random + 1 DEFUSE)
     * @ensures Deck has EXPLODING cards = (numberOfPlayers - 1)
     */
    public void setUp() {
        this.removeExplodeAndDefuseCards();
        this.dealCardsToPlayers();
        this.insertExplodeCards();
    }

    /**
     * Combines STEP 1 & 2 from the rules, removing the Exploding Kittens (4)
     *      and Defuses (6) cards.
     * @ensures Deck has no more Explode / Defuse cards.
     */
    public void removeExplodeAndDefuseCards() {
        deck.getDeckArray().removeIf(card -> (
                card == Card.EXPLODE
                        || card == Card.DEFUSE));
    }

    /**
     * Deals the right number of cards to players. That is, each player gets
     *      a Defuse card. Then, depending on NUMBER_OF_PLAYERS, there are inserted 1 or 2
     *      Defuse cards back in the deck. Then, each player gets 7 cards from the deck, for
     *      a total of 8.
     * @ensures Each player has 8 cards && (1 or 2) Defuse cards in deck.
     */
    public void dealCardsToPlayers() {
        players.forEach(player -> player.getHand().add(Card.DEFUSE));

        if (NUMBER_OF_PLAYERS == 5) {
            this.deck.getDeckArray().add(Card.DEFUSE);
        } else {
            this.deck.getDeckArray().add(Card.DEFUSE);
            this.deck.getDeckArray().add(Card.DEFUSE);
        }
        Collections.shuffle(deck.getDeckArray());
        for (int cardIndex = 0; cardIndex < Player.MAX_NUMBER_OF_CARDS_IN_HAND_WITHOUT_DEFUSE; cardIndex++) {
            players.forEach(player -> player.getHand().add(deck.getLastCardAndRemove()));
        }
    }

    /**
     * Inserts NUMBER_OF_PLAYERS - 1 (such that there remains a winner) Exploding
     *      cards back into the deck. The deck is then shuffled and ready to be played.
     */
    public void insertExplodeCards() {
        for(int cardIndex = 0; cardIndex < NUMBER_OF_PLAYERS - 1; cardIndex++) {
            deck.getDeckArray().add(Card.EXPLODE);
        }
        Collections.shuffle(deck.getDeckArray());
    }

    /* ********* COMMAND PROCESSING ********* */
    /**
     * From Computer & Human
     * @param move
     */
    public void processMove(String move) {
        String[] commands = move.split(" ");

        switch (commands[0].toLowerCase()) {
            case "play" -> processPlay(commands);
            case "draw" -> {
                if (commands.length == 1) // "draw" does not accept any arguments
                    processDraw();
                else
                    GameController.raiseWarningAtDrawIllegalArgs();
            }
            default -> GameController.raiseWarningWrongInput(move);
        }
    }

    /**
     * @invariant command[0] = "play
     */
    private void processPlay(String[] commands) {
        Card firstCard = null;

        if (!(isValid(commands[1]))) {
            GameController.raiseWarningWrongInput(
                    commands[1].toUpperCase() + " is not a valid move." +
                            "\n[!] If it is a valid card, you don't have such a card in your hand.");
        } else {
            firstCard = Card.valueOf(commands[1].toUpperCase());
            switch (firstCard) {
                case ATTACK  -> processAttackCard();
                case FAVOR   -> processFavorCard();
                case NOPE    -> processNopeCard();
                case SHUFFLE -> processShuffleCard();
                case SKIP    -> processSkipCard();
                case FUTURE  -> processFutureCard();
                case TACOCAT, CATTERMELLON, POTATO, BEARD, RAINBOW
                             -> processNormalCard(firstCard, commands.length);
            }
        }
    }

    public void processAttackCard() {

    }

    public void processFavorCard() {

    }

    public void processNopeCard() {

    }

    public void processShuffleCard() {
        if (currentPlayer.hasTurnsToStay()) {
            GameController.raiseWarningHasTurnsToStay(currentPlayer.getTurnsToStay());
        } else {
            Collections.shuffle(this.deck.getDeckArray());
            GameController.promptShuffleConfirmation();
            currentPlayer.getHand().remove(Card.SHUFFLE);
            discardPile.add(Card.SHUFFLE);
        }
    }

    public void processSkipCard() {

    }

    public void processFutureCard() {
        if (currentPlayer.hasTurnsToStay()) {
            GameController.raiseWarningHasTurnsToStay(currentPlayer.getTurnsToStay());
        } else {
            GameController.promptLastThreeCardsFromDeck(
                    deck.getDeckArray().get(deck.getDeckArray().size() - 1),
                    deck.getDeckArray().get(deck.getDeckArray().size() - 2),
                    deck.getDeckArray().get(deck.getDeckArray().size() - 3)
            );
            currentPlayer.getHand().remove(Card.FUTURE);
            discardPile.add(Card.FUTURE);
        }
    }

    public void processNormalCard(Card card, int doubleOrTripleCombo) {

    }

    public boolean isValid(String cardToCheck) {
        return ((Arrays.stream(Card.values())
                .anyMatch(card -> cardToCheck.equalsIgnoreCase(card.name())))
                && currentPlayer.getHand().contains(Card.valueOf(cardToCheck.toUpperCase()))
                && !Card.valueOf(cardToCheck.toUpperCase()).equals(Card.DEFUSE)
                && !Card.valueOf(cardToCheck.toUpperCase()).equals(Card.EXPLODE));
    }

    private void processDraw() {
        boolean hasDefuse = currentPlayer.getHand().contains(Card.DEFUSE);
        int desiredIndex;

        if (deck.getLastCard() == Card.EXPLODE && hasDefuse) {
            desiredIndex = GameController.askIndexOfReinsertingExplode();
            deck.getDeckArray().remove(Card.EXPLODE);
            deck.getDeckArray().add(desiredIndex, Card.EXPLODE);
            currentPlayer.getHand().remove(Card.DEFUSE);
            discardPile.add(Card.DEFUSE);
        } else if (deck.getLastCard() == Card.EXPLODE && !hasDefuse) {
            GameController.informPlayerKick();
            players.remove(currentPlayer);
            deck.getDeckArray().remove(Card.EXPLODE);
            checkHasWinner();
        } else if (deck.getLastCard() != Card.EXPLODE) {
            currentPlayer.getHand().add(deck.getLastCardAndRemove());
        }
    }


    /* ********* CARD PROCESSING ********* */


    /* ********* MISCELLANEOUS ********* */
    public void checkHasWinner() {
        if (players.size() == 1 ) {
            GameController.informWinner();
            incrementPlayerTurnIndex();
            System.exit(0);
        }
    }


    //
    //
    //
    //
    //
    //
    //
    //
    //
    public static void main(String[] args) {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("BOT"));
        players.add(new Player("Alex"));
//        players.add(new Player("BOT"));
//        players.add(new Player("Mircea"));
        Game game = new Game(players);
        game.setUp();

        System.out.println(players.get(0) + " " + game.players.get(0).getHand());
        System.out.println(players.get(1) + " " + game.players.get(1).getHand());
//        System.out.println(players.get(2) + " " + game.players.get(2).getHand());
//        System.out.println(players.get(3) + " " + game.players.get(3).getHand());

        System.out.println(game.deck.getDeckArray().size());
        System.out.println(game.deck.getDeckArray());
    }

}
