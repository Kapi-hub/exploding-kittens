package model;

public class ComputerPlayer extends Player {
    /**
     * The computer is developed such that it knows only the:
     *  - number of cards in deck
     *  - cards played in the game
     *  It might do card counting, depending on how much time I have.
     * @param name
     */
    public ComputerPlayer(String name) {
        super(name);
    }
    public ComputerPlayer() {
        super();
    }

    public String doMove(Game game) {
        return "draw";
    }
}
