import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private static final int MAX_NUMBER_OF_CARDS_IN_DECK = 56;
    private static final int MAX_ALLOWED_PLAYERS = 5;

    private Game game;
    private Game game2;
    private ArrayList<Player> players;
    private ArrayList<Player> players2;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        for(int index = 0; index < 3; index++)
            players.add(new HumanPlayer());
        game = new Game(players);

        players2 = new ArrayList<>();
        for(int index = 0; index < MAX_ALLOWED_PLAYERS; index++)
            players2.add(new ComputerPlayer());
        game2 = new Game(players2);
    }

    @Test
    @DisplayName("Tests if the object is not null && of type Deck.")
    // Whole deck is tedted in class TestDeck.
    void getDeckObject() {
        assertNotNull(game.getDeckObject(), "Deck object is null.");
        assertTrue(game.getDeckObject() instanceof Deck, "deck is not" +
                "an instance of class Deck.");
    }

    @Test
    @DisplayName("Tests if 0 <= index of starting player <= number of players")
    void getIndexOfPlayerToPlayFirst() {
        assertTrue(game.getIndexOfPlayerToPlayFirst() >= 0,
                "Index of player to start is less than 1!");
        assertTrue(game.getIndexOfPlayerToPlayFirst()
                <= game.getNUMBER_OF_PLAYERS(),
                "Index of player to start is bigger than the number of " +
                        "players.");
    }

    @Test
    @DisplayName("Tests if, upon creation, game's players are the ones passed" +
            "as argument")
    void getPlayers() {
        assertEquals(game.getPlayers(), players,
                "The game does not have the same players as indicated.");
        assertEquals(game.getPlayers().size(), players.size(),
                "The number of game's players does not match that of" +
                        "the indicated ones upon game construction.");
    }

    @Test
    @DisplayName("Tests if EXPLODE (4) and DEFUSE (6) cards are initially " +
            "deleted.")
    void removeExplodeAndDefuseCards() {
        game.removeExplodeAndDefuseCards();
        assertEquals(MAX_NUMBER_OF_CARDS_IN_DECK - 4 - 6,
                game.getDeckObject().getDeckAsArrayList().size(),
                "Number of remaining cards is different than 46.");
    }

    @Test
    @DisplayName("Tests if each player has certain cards and if each player" +
            "has the right number of cards, in hand.")
    void dealCardsToPlayers() {
        game.removeExplodeAndDefuseCards();
        game.dealCardsToPlayers();

        List<Player> playersWithDefuse = players.stream()
                .filter(player -> player.getHand().contains(Card.SpecialCard.DEFUSE))
                .collect(Collectors.toList());

        assertEquals(players.size(), playersWithDefuse.size(),
                "Not all players have Defuse cards in hands.");

        /*
        It should be less or equal than 2 (not equal to 2) because, if the
            shuffle puts the DEFUSE cards into first NUMBER_OF_PLAYERS * 7
             cards, then a player will actually draw the DEFUSE card upon
             distributing the cards.
         */
        assertTrue(Collections.frequency(
                game.getDeckObject().getDeckAsArrayList(),
                Card.SpecialCard.DEFUSE) <= 2,
                "There are less or more than 2 DEFUSE cards in the deck.");

        for (Player player : players)
            assertEquals(8, player.getHand().stream().count());
    }

    @Test
    @DisplayName("Tests if deck has two DEFUSEs given 5 players are playing.")
    void dealCardsToPlayersIf5PlayersArePlaying() {
        game2.removeExplodeAndDefuseCards();
        game2.dealCardsToPlayers();

        assertTrue(Collections.frequency(
                game2.getDeckObject().getDeckAsArrayList(),
                Card.SpecialCard.DEFUSE) <= 1,
                "There are more than 1 DEFUSEs given 5 players ");
    }

    @Test
    @DisplayName("Tests if the current number of EXPLODE Cards are inserted" +
            "correctly into the deck before randomly picking a player.")
    void insertExplodeCards() {
        game.removeExplodeAndDefuseCards();
        game.dealCardsToPlayers();
        game.insertExplodeCards();

        game2.removeExplodeAndDefuseCards();
        game2.dealCardsToPlayers();
        game2.insertExplodeCards();

        assertEquals(players.size() - 1, Collections.frequency(
                game.getDeckObject().getDeckAsArrayList(),
                Card.SpecialCard.EXPLODE));
        assertEquals(players2.size() - 1, Collections.frequency(
                game2.getDeckObject().getDeckAsArrayList(),
                Card.SpecialCard.EXPLODE));
    }
}