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

package de.kcodeyt.headsdb.lang;

import lombok.Getter;

import java.util.Locale;

/**
 * @author Kevims KCodeYT
 * @version 1.0
 */
public enum TranslationKey {

    CONSOLE_USES_PLAYER_COMMAND,
    DATABASE_RELOAD_START,
    DATABASE_RELOAD_FAILED,
    DATABASE_RELOAD_SUCCESS,
    GAVE_RANDOM_HEADS,
    CONFIG_FORM_TITLE,
    CONFIG_FORM_PAGE_SIZE,
    CONFIG_FORM_PAGE_SIZE_CHANGED,
    DATABASE_FORM_TITLE,
    DATABASE_FORM_SEARCH_HEAD,
    SEARCH_HEAD_FORM_TITLE,
    SEARCH_HEAD_FORM_INPUT_FIELD,
    SEARCH_HEAD_FORM_FAILED_EMPTY,
    SEARCH_HEAD_SEARCHING_FORM_TITLE,
    CATEGORY_ALPHABET,
    CATEGORY_ANIMALS,
    CATEGORY_BLOCKS,
    CATEGORY_DECORATION,
    CATEGORY_FOOD_DRINKS,
    CATEGORY_HUMANS,
    CATEGORY_HUMANOID,
    CATEGORY_MISCELLANEOUS,
    CATEGORY_MONSTERS,
    CATEGORY_PLANTS,
    CATEGORY_ALPHABET_TITLE,
    CATEGORY_ANIMALS_TITLE,
    CATEGORY_BLOCKS_TITLE,
    CATEGORY_DECORATION_TITLE,
    CATEGORY_FOOD_DRINKS_TITLE,
    CATEGORY_HUMANS_TITLE,
    CATEGORY_HUMANOID_TITLE,
    CATEGORY_MISCELLANEOUS_TITLE,
    CATEGORY_MONSTERS_TITLE,
    CATEGORY_PLANTS_TITLE,
    SELECT_A_PAGE_TITLE,
    PAGE_BUTTON,
    SELECT_SUB_PAGE_TITLE,
    SUB_PAGE_BUTTON;

    @Getter
    private final String key;

    TranslationKey() {
        this.key = this.name().toLowerCase(Locale.ROOT).replace("_", "-");
    }

}
