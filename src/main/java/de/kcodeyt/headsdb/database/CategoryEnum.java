package de.kcodeyt.headsdb.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryEnum {

    ALPHABET("Alphabet", "alphabet"),
    ANIMALS("Animals", "animals"),
    BLOCKS("Blocks", "blocks"),
    DECORATION("Decoration", "decoration"),
    FOOD_DRINKS("Food & Drinks", "food-drinks"),
    HUMANS("Humans", "humans"),
    HUMANOID("Humanoid", "humanoid"),
    MISCELLANEOUS("Miscellaneous", "miscellaneous"),
    MONSTERS("Monsters", "monsters"),
    PLANTS("Plants", "plants");

    private final String displayName;
    private final String identifier;

}
