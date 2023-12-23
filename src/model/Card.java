package model;

public interface Card {
    enum SpecialCard implements Card {
        EXPLODE,
        DEFUSE,
        ATTACK,
        FAVOR,
        NOPE,
        SHUFFLE,
        SKIP,
        FUTURE
    }

    enum RegularCard implements Card {
        TACOCAT,
        CATTERMELLON,
        POTATO,
        BEARD,
        RAINBOW
    }
}
