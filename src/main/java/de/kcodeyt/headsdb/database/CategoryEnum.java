/*
 * Copyright 2022 KCodeYT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kcodeyt.headsdb.database;

import de.kcodeyt.headsdb.lang.TranslationKey;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryEnum {

    ALPHABET(TranslationKey.CATEGORY_ALPHABET, TranslationKey.CATEGORY_ALPHABET_TITLE, "alphabet"),
    ANIMALS(TranslationKey.CATEGORY_ANIMALS, TranslationKey.CATEGORY_ANIMALS_TITLE, "animals"),
    BLOCKS(TranslationKey.CATEGORY_BLOCKS, TranslationKey.CATEGORY_BLOCKS_TITLE, "blocks"),
    DECORATION(TranslationKey.CATEGORY_DECORATION, TranslationKey.CATEGORY_DECORATION_TITLE, "decoration"),
    FOOD_DRINKS(TranslationKey.CATEGORY_FOOD_DRINKS, TranslationKey.CATEGORY_FOOD_DRINKS_TITLE, "food-drinks"),
    HUMANS(TranslationKey.CATEGORY_HUMANS, TranslationKey.CATEGORY_HUMANS_TITLE, "humans"),
    HUMANOID(TranslationKey.CATEGORY_HUMANOID, TranslationKey.CATEGORY_HUMANOID_TITLE, "humanoid"),
    MISCELLANEOUS(TranslationKey.CATEGORY_MISCELLANEOUS, TranslationKey.CATEGORY_MISCELLANEOUS_TITLE, "miscellaneous"),
    MONSTERS(TranslationKey.CATEGORY_MONSTERS, TranslationKey.CATEGORY_MONSTERS_TITLE, "monsters"),
    PLANTS(TranslationKey.CATEGORY_PLANTS, TranslationKey.CATEGORY_PLANTS_TITLE, "plants");

    private final TranslationKey buttonTranslationKey;
    private final TranslationKey titleTranslationKey;
    private final String identifier;

}
