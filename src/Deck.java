import java.util.*;

public class Deck {
    private static final int CARD_INSTANCES_FOUR = 4;
    private static final int CARD_INSTANCES_FIVE = 5;
    private static final int CARD_INSTANCES_SIX = 6;
    // Set to ArrayList because there is no intention of switching it to another implementation later on.
    private ArrayList<Card> deck = new ArrayList<>();

    /* ************************************
              GETTERS & SETTERS
    ************************************ */

     public ArrayList<Card> getDeckAsArrayList() {
        return deck;
    }

    /* ************************************
                CONSTRUCTORS
    ************************************ */

    public Deck() {
        this.deck = generateDeck();
    }

    /* ************************************
                    METHODS
    ************************************ */

    /**
     * Generates a deck of 56 cards for the game.
     * In each starting cards, there are:
     *  - <4 CARDS> of EXPLODE, ATTACK, SKIP, FAVOR, SHUFFLE, TACOCAT, CATTERMELLON, POTATO, BEARD, RAINBOW
     *  - <5 CARDS> of FUTURE (See the future), NOPE
     *  - <6 CARDS> of DEFUSE
     * @return ArrayList<Card> this.deck
     * @ensures A deck which consists of 56 cards.
     */
    public ArrayList<Card> generateDeck() {
        // Generate the cards that have 4 instances in the deck. There are 10 such types of cards.
        for (int cardInstance = 0; cardInstance < CARD_INSTANCES_FOUR; cardInstance++) { // 4 instances of each card.
            deck.add(Card.SpecialCard.EXPLODE);
            deck.add(Card.SpecialCard.ATTACK);
            deck.add(Card.SpecialCard.SKIP);
            deck.add(Card.SpecialCard.FAVOR);
            deck.add(Card.SpecialCard.SHUFFLE);
            deck.add(Card.RegularCard.TACOCAT);
            deck.add(Card.RegularCard.CATTERMELLON);
            deck.add(Card.RegularCard.POTATO);
            deck.add(Card.RegularCard.BEARD);
            deck.add(Card.RegularCard.RAINBOW);
        }

        // Generate the cards that have 5 instances in the deck. There are 2 such types of cards.
        for (int cardInstance = 0; cardInstance < CARD_INSTANCES_FIVE; cardInstance++) {
            deck.add(Card.SpecialCard.FUTURE);
            deck.add(Card.SpecialCard.NOPE);
        }

        // Generate the cards that have 6 instances in the deck. There are 1 such types of card.
        for (int cardInstance = 0; cardInstance < CARD_INSTANCES_SIX; cardInstance++) {
            deck.add(Card.SpecialCard.DEFUSE);
        }
        Collections.shuffle(deck);
        return deck;
    }

    public Card getLastCard() {
        Card lastCardFromDeck = deck.get(deck.size() - 1);
        deck.remove(deck.size() - 1);
        return lastCardFromDeck;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < deck.size(); i++) {
            result += deck.get(i) + "\n";
        }
        return result;
    }

    public static void main(String[] args) {
        Deck deck = new Deck();
        System.out.println(deck.getDeckAsArrayList().size());
    }
}
