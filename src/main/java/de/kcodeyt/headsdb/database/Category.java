package de.kcodeyt.headsdb.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
class Category {

    private final CategoryEnum categoryEnum;
    private final String displayName;
    private final String displaySkin;
    private final List<HeadEntry> entries;

}
