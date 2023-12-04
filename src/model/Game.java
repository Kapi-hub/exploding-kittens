package model;

import java.util.ArrayList;
import java.util.Collections;

public class Game {
    private Deck deck;
    private ArrayList<Player> players = new ArrayList<>();

    private final int NUMBER_OF_PLAYERS;
    private final int indexOfPlayerToPlayFirst;

    /* ************************************
              GETTERS & SETTERS
    ************************************ */
    public Deck getDeckObject() {
        return deck;
    }

    public int getIndexOfPlayerToPlayFirst() {
        return indexOfPlayerToPlayFirst;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getNUMBER_OF_PLAYERS() { return NUMBER_OF_PLAYERS; }

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
        indexOfPlayerToPlayFirst = (int) (Math.random() * NUMBER_OF_PLAYERS);
        deck = new Deck();
    }

    /* ************************************
                    METHODS
    ************************************ */

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
        deck.getDeckAsArrayList().removeIf(card -> (
                card == Card.SpecialCard.EXPLODE
                        || card == Card.SpecialCard.DEFUSE));
    }

    /**
     * Deals the right number of cards to players. That is, each player gets
     *      a Defuse card. Then, depending on NUMBER_OF_PLAYERS, there are inserted 1 or 2
     *      Defuse cards back in the deck. Then, each player gets 7 cards from the deck, for
     *      a total of 8.
     * @ensures Each player has 8 cards && (1 or 2) Defuse cards in deck.
     */
    public void dealCardsToPlayers() {
        players.forEach(player -> player.getHand().add(Card.SpecialCard.DEFUSE));

        if (NUMBER_OF_PLAYERS == 5) {
            this.deck.getDeckAsArrayList().add(Card.SpecialCard.DEFUSE);
        } else {
            this.deck.getDeckAsArrayList().add(Card.SpecialCard.DEFUSE);
            this.deck.getDeckAsArrayList().add(Card.SpecialCard.DEFUSE);
        }
        Collections.shuffle(deck.getDeckAsArrayList());
        for (int cardIndex = 0; cardIndex < Player.MAX_NUMBER_OF_CARDS_IN_HAND_WITHOUT_DEFUSE; cardIndex++) {
            players.forEach(player -> player.getHand().add(deck.getLastCard()));
        }
    }

    /**
     * Inserts NUMBER_OF_PLAYERS - 1 (such that there remains a winner) Exploding
     *      cards back into the deck. The deck is then shuffled and ready to be played.
     */
    public void insertExplodeCards() {
        for(int cardIndex = 0; cardIndex < NUMBER_OF_PLAYERS - 1; cardIndex++) {
            deck.getDeckAsArrayList().add(Card.SpecialCard.EXPLODE);
        }
        Collections.shuffle(deck.getDeckAsArrayList());
    }

    public static void main(String[] args) {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("BOT"));
        players.add(new Player("Alex"));
        players.add(new Player("BOT"));
        players.add(new Player("Mircea"));
        Game game = new Game(players);
        game.setUp();

        System.out.println(players.get(0) + " " + game.players.get(0).getHand());
        System.out.println(players.get(1) + " " + game.players.get(1).getHand());
        System.out.println(players.get(2) + " " + game.players.get(2).getHand());
        System.out.println(players.get(3) + " " + game.players.get(3).getHand());

        System.out.println(game.deck.getDeckAsArrayList());

    }

}
