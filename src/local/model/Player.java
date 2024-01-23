package local.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Player {
    protected static int MAX_NUMBER_OF_CARDS_IN_HAND = 8;
    protected static int MAX_NUMBER_OF_CARDS_IN_HAND_WITHOUT_DEFUSE = 7;
    protected ArrayList<Card> handOfCards;
    private String name;
    private int turnsToStay = 0;


    /* ************************************
              GETTERS & SETTERS
    ************************************ */

    public ArrayList<Card> getHand() {
        return this.handOfCards;
    }

    public String getName() {
        return name;
    }

    public int getTurnsToStay() {return turnsToStay;}

    public boolean hasTurnsToStay()  {return (turnsToStay != 0);}

    public void setTurnsToStay(int turns) {turnsToStay += turns;}
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
