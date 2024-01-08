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
//     * Performs the move that the player makes, whether the player
//     * is a human or a bot.
//     * @param move
//     */
//    public Card doMove(String move, Game game) {
//        List<String> commands = new ArrayList<>(Arrays.asList(move.split(" ")));
//        String playOrDraw = commands.get(0).toLowerCase();
//        switch (playOrDraw) {
//            case "play":
//                Card card = Card.valueOf(commands.get(1).toUpperCase());
//                return processCard(card, game);
//            case "draw":
//                return super.draw(game);
//        }
//        return null;
//    }

    /**
     * Private because it is accessed by doMove()
     */
    private Card processCard(Card card, Game game) {
        switch (card) {
            case ATTACK:
                System.out.println("yes");
                break;
            case FAVOR:
                break;
            case NOPE:
                System.out.println("yes");
                break;
            case SHUFFLE:
                break;
            case SKIP:
                break;
            case FUTURE:
                System.out.println("yes");
                break;

        }
        return null;
    }
}
