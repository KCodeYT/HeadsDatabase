package de.kcodeyt.headsdb.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class HeadEntry {

    private final String name;
    private final String id;
    private final String texture;
    private final String tags;

}
