import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {
    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    @Test
    @DisplayName("Tests if the generated deck contains:" +
            "- 56 cards;" +
            "- correct number of instances;")
    void generateDeckTest() {
        assertEquals(56, deck.getDeckAsArrayList().size());

        // Checking some values. Given the above test passes (56 cards in deck)
        // and there are the same number of Defuse (6) and Exploding (4) cards,
        // there is no point in checking for each type of card.
        int numberOfExplodingKittens = 0;
        int numberOfDefuse = 0;
        for ( Card card : deck.getDeckAsArrayList()) {
            if (card == Card.SpecialCard.EXPLODE )
                numberOfExplodingKittens++;
            if (card == Card.SpecialCard.DEFUSE)
                numberOfDefuse++;
        }
        assertEquals(4, numberOfExplodingKittens);
        assertEquals(6, numberOfDefuse);
    }

}