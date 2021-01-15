package de.kcodeyt.headsdb.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
class Category {

    private final Categories categories;
    private final String displayName;
    private final String displaySkin;
    private final List<DBSkin> skins;

}
