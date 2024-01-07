package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HumanPlayer extends Player {
    public HumanPlayer(String name) {
        super(name);
    }
    public HumanPlayer() {
        super();
    }



    /**
     * Performs the move that the player makes, whether the player
     * is a human or a bot.
     * @param move
     */
    public void doMove(String move, Game game) {
        List<String> commands = new ArrayList<>(Arrays.asList(move.split(" ")));
        String playOrDraw = commands.get(0).toLowerCase();
        switch (playOrDraw) {
            case "play":
                Card card = Card.SpecialCard.valueOf(commands.get(1).toUpperCase());
            break;
            case "draw":
                this.handOfCards.add(game.getDeckObject().getLastCardAndRemove());
                break;
        }
    }
}
