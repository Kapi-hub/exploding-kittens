package network.model;

import java.util.ArrayList;

public class Player {
    protected static int MAX_NUMBER_OF_CARDS_IN_HAND = 8;
    protected static int MAX_NUMBER_OF_CARDS_IN_HAND_WITHOUT_DEFUSE = 7;
    protected ArrayList<Card> handOfCards;
    private String name;
    private int turnsToPlay = 0;
    private boolean hasInsertedExplode = false;


    /* ************************************
              GETTERS & SETTERS
    ************************************ */

    public ArrayList<Card> getHand() {
        return this.handOfCards;
    }

    public String getName() {
        return name;
    }

    public int getTurnsToPlay() {return turnsToPlay;}

    public boolean hasTurnsToPlay()  {return (turnsToPlay != 0);}

    public void setTurnsToPlay(int turns) {
        turnsToPlay += turns;}

    public boolean isHasInsertedExplode() {
        return hasInsertedExplode;
    }

    public void setHasInsertedExplode(boolean hasInsertedExplode) {
        this.hasInsertedExplode = hasInsertedExplode;
    }

    /* ************************************
                CONSTRUCTORS
    ************************************ */

    public Player() {
        this.handOfCards = new ArrayList<>();
    }

    public Player(String name) {
        this();
        this.name = name;
    }

    /* ************************************
                    METHODS
    ************************************ */

    @Override
    public String toString() {
        return (this.name + ": ");
    }

}
