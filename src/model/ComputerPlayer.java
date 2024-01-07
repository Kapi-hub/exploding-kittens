package model;

public class ComputerPlayer extends Player {
    public ComputerPlayer(String name) {
        super(name);
    }
    public ComputerPlayer() {
        super();
    }

    public void doMove(Game game) {
        this.handOfCards.add(game.getDeckObject().getLastCardAndRemove());
    }
}
