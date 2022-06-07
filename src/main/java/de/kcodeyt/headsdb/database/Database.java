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

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import de.kcodeyt.heads.api.HeadAPI;
import de.kcodeyt.heads.api.SkullOwnerResolveMethod;
import de.kcodeyt.heads.util.PluginHolder;
import de.kcodeyt.heads.util.ScheduledFuture;
import de.kcodeyt.headsdb.HeadsDB;
import de.kcodeyt.headsdb.lang.Language;
import de.kcodeyt.headsdb.lang.TranslationKey;
import de.kcodeyt.headsdb.util.FormAPI;
import de.kcodeyt.headsdb.util.HeadRender;
import lombok.Getter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletionException;

@Getter
public class Database {

    private static final String API_URL = "https://minecraft-heads.com/scripts/api.php";
    private static final Gson GSON = new Gson();

    private final HeadsDB headsDB;
    private final Map<String, Integer> pageCount;
    private final List<Category> categories;
    private final List<HeadEntry> headEntries;
    private ScheduledFuture<Boolean> loadFuture;

    public Database(HeadsDB headsDB) {
        this.headsDB = headsDB;
        this.pageCount = new HashMap<>();
        this.categories = new ArrayList<>();
        this.headEntries = new ArrayList<>();
    }

    public ScheduledFuture<Boolean> reload() {
        if(this.loadFuture != null)
            return this.loadFuture;
        return this.load(true);
    }

    public ScheduledFuture<Boolean> load() {
        return this.load(false);
    }

    private ScheduledFuture<Boolean> load(boolean clear) {
        if(this.loadFuture != null) return this.loadFuture;

        final List<HeadEntry> localHeadEntries = new ArrayList<>();
        final List<Category> localCategories = new ArrayList<>();
        return this.loadFuture = ScheduledFuture.supplyAsync(() -> {
            try {
                for(final CategoryEnum category : CategoryEnum.values()) {
                    final HttpURLConnection connection = (HttpURLConnection) new URL(API_URL + "?cat=" + category.getIdentifier() + "&tags=true").openConnection();
                    connection.setRequestProperty("User-Agent", "Chrome");
                    connection.connect();

                    if(connection.getResponseCode() == 200) {
                        try(final InputStream inputStream = connection.getInputStream();
                            final Reader reader = new InputStreamReader(inputStream)) {
                            final List<Map<String, String>> values = GSON.<List<Map<String, String>>>fromJson(reader, List.class);
                            final List<HeadEntry> headEntries = new ArrayList<>();
                            for(Map<String, String> map : values)
                                headEntries.add(new HeadEntry(map.get("name"), map.get("uuid"), map.get("value"), map.get("tags")));
                            localHeadEntries.addAll(headEntries);
                            localCategories.add(new Category(category, Iterables.getLast(headEntries).getTexture(), Collections.unmodifiableList(headEntries)));
                        }
                    }

                    connection.disconnect();
                }

                return true;
            } catch(Exception e) {
                throw new CompletionException(e);
            }
        }).whenComplete((result, error) -> {
            this.loadFuture = null;
            if(result) {
                if(clear) {
                    this.categories.clear();
                    this.headEntries.clear();
                }

                this.headEntries.addAll(localHeadEntries);
                this.categories.addAll(localCategories);
            }
        });
    }

    private <T> List<List<T>> toPages(List<T> entries, int count) {
        final int dbSize = entries.size();
        if(dbSize <= 0)
            return Collections.emptyList();
        final List<List<T>> pages = new ArrayList<>();
        final int chunks = (dbSize - 1) / count;
        for(int i = 0; i <= chunks; i++)
            pages.add(entries.subList(i * count, i == chunks ? dbSize : (i + 1) * count));
        return Collections.unmodifiableList(pages);
    }

    public void showForm(Player player) {
        final Language language = this.headsDB.getLanguage();

        final FormWindowSimple categoriesForm = new FormWindowSimple(language.translate(player, TranslationKey.DATABASE_FORM_TITLE), "");
        final List<Category> categories = Collections.unmodifiableList(new ArrayList<>(this.categories));
        categoriesForm.addButton(new ElementButton(language.translate(player, TranslationKey.DATABASE_FORM_SEARCH_HEAD), HeadRender.createButtonImageById(HeadsDB.MHF_QUESTION_TEXTURE_ID)));
        for(final Category category : categories)
            categoriesForm.addButton(new ElementButton(language.translate(player, category.getCategoryEnum().getButtonTranslationKey()), HeadRender.createButtonImage(category.getDisplaySkin())));
        FormAPI.create(player, categoriesForm, () -> {
            if(categoriesForm.wasClosed())
                return;

            if(categoriesForm.getResponse().getClickedButtonId() == 0) {
                final FormWindowCustom searchForm = new FormWindowCustom(language.translate(player, TranslationKey.SEARCH_HEAD_FORM_TITLE));
                searchForm.addElement(new ElementInput(language.translate(player, TranslationKey.SEARCH_HEAD_FORM_INPUT_FIELD)));
                FormAPI.create(player, searchForm, () -> {
                    if(searchForm.wasClosed()) {
                        FormAPI.createLast(player, categoriesForm);
                        return;
                    }

                    final String searchInput = TextFormat.clean(Objects.toString(searchForm.getResponse().getInputResponse(0), "")).trim();
                    if(searchInput.isEmpty()) {
                        player.sendMessage(language.translate(player, TranslationKey.SEARCH_HEAD_FORM_FAILED_EMPTY));
                        return;
                    }

                    final String[] searchArgs = Arrays.stream(searchInput.split(",")).
                            map(String::trim).map(String::toLowerCase).toArray(String[]::new);

                    final List<HeadEntry> foundEntries = new ArrayList<>();
                    next_head:
                    for(final HeadEntry headEntry : this.headEntries) {
                        final String name = headEntry.getName().toLowerCase(Locale.ROOT);
                        final String[] tags = headEntry.getTags() != null ?
                                Arrays.stream(headEntry.getTags().split(",")).
                                        map(String::toLowerCase).toArray(String[]::new) : null;

                        for(final String searchArg : searchArgs) {
                            if(name.startsWith(searchArg) || name.endsWith(searchArg) || name.contains(searchArg)) {
                                foundEntries.add(headEntry);
                                continue next_head;
                            } else if(tags != null)
                                for(final String tag : tags) {
                                    if(tag.startsWith(searchArg) || tag.endsWith(searchArg) || tag.contains(searchArg)) {
                                        foundEntries.add(headEntry);
                                        continue next_head;
                                    }
                                }
                        }
                    }

                    this.showForm(player, searchForm, foundEntries, language.translate(player, TranslationKey.SEARCH_HEAD_SEARCHING_FORM_TITLE, searchInput));
                });
            } else {
                final Category category = categories.get(categoriesForm.getResponse().getClickedButtonId() - 1);
                if(category == null) return;

                this.showForm(player, categoriesForm, category.getEntries(), language.translate(player, category.getCategoryEnum().getTitleTranslationKey()));
            }
        });
    }

    private void showForm(Player player, FormWindow lastWindow, List<HeadEntry> headEntries, String title) {
        final Language language = this.headsDB.getLanguage();

        final int pageCount = this.pageCount.getOrDefault(player.getName(), 40);
        if(headEntries.size() > pageCount * pageCount) {
            final FormWindowSimple pagesForm = new FormWindowSimple(language.translate(player, TranslationKey.SELECT_A_PAGE_TITLE), "");
            final List<List<List<HeadEntry>>> pages = this.toPages(this.toPages(headEntries, pageCount), pageCount);
            for(int i = 0; i < pages.size(); i++)
                pagesForm.addButton(new ElementButton(language.translate(player, TranslationKey.PAGE_BUTTON, (i + 1)), HeadRender.createButtonImage(Iterables.getLast(Iterables.getLast(pages.get(i))).getTexture())));
            FormAPI.create(player, pagesForm, () -> {
                if(pagesForm.wasClosed()) {
                    FormAPI.createLast(player, lastWindow);
                    return;
                }

                final FormWindowSimple subPagesForm = new FormWindowSimple(language.translate(player, TranslationKey.SELECT_SUB_PAGE_TITLE), "");
                final List<List<HeadEntry>> subPages = pages.get(pagesForm.getResponse().getClickedButtonId());
                for(int i = 0; i < subPages.size(); i++)
                    subPagesForm.addButton(new ElementButton(language.translate(player, TranslationKey.SUB_PAGE_BUTTON, (i + 1)), HeadRender.createButtonImage(Iterables.getLast(subPages.get(i)).getTexture())));
                FormAPI.create(player, subPagesForm, () -> {
                    if(subPagesForm.wasClosed()) {
                        FormAPI.createLast(player, pagesForm);
                        return;
                    }

                    final List<HeadEntry> headEntries0 = subPages.get(subPagesForm.getResponse().getClickedButtonId());
                    if(headEntries0 == null) return;

                    final FormWindowSimple subForm = new FormWindowSimple(title, "");
                    for(final HeadEntry headEntry : headEntries0)
                        subForm.addButton(new ElementButton(headEntry.getName(), HeadRender.createButtonImage(headEntry.getTexture())));
                    FormAPI.create(player, subForm, () -> {
                        if(subForm.wasClosed()) {
                            FormAPI.createLast(player, subPagesForm);
                            return;
                        }

                        final HeadEntry headEntry = headEntries0.get(subForm.getResponse().getClickedButtonId());
                        if(headEntry == null) return;

                        this.giveItem(player, headEntry);
                    });
                });
            });
        } else if(headEntries.size() > pageCount) {
            final FormWindowSimple pagesForm = new FormWindowSimple(language.translate(player, TranslationKey.SELECT_A_PAGE_TITLE), "");
            final List<List<HeadEntry>> pages = this.toPages(headEntries, pageCount);
            for(int i = 0; i < pages.size(); i++)
                pagesForm.addButton(new ElementButton(language.translate(player, TranslationKey.PAGE_BUTTON, (i + 1)), HeadRender.createButtonImage(Iterables.getLast(pages.get(i)).getTexture())));
            FormAPI.create(player, pagesForm, () -> {
                if(pagesForm.wasClosed()) {
                    FormAPI.createLast(player, lastWindow);
                    return;
                }

                final List<HeadEntry> headEntries0 = pages.get(pagesForm.getResponse().getClickedButtonId());
                if(headEntries0 == null) return;

                final FormWindowSimple subForm = new FormWindowSimple(title, "");
                for(final HeadEntry headEntry : headEntries0)
                    subForm.addButton(new ElementButton(headEntry.getName(), HeadRender.createButtonImage(headEntry.getTexture())));
                FormAPI.create(player, subForm, () -> {
                    if(subForm.wasClosed()) {
                        FormAPI.createLast(player, pagesForm);
                        return;
                    }

                    final HeadEntry headEntry = headEntries0.get(subForm.getResponse().getClickedButtonId());
                    if(headEntry == null) return;

                    this.giveItem(player, headEntry);
                });
            });
        } else {
            final FormWindowSimple subForm = new FormWindowSimple(title, "");
            for(final HeadEntry headEntry : headEntries)
                subForm.addButton(new ElementButton(headEntry.getName(), HeadRender.createButtonImage(headEntry.getTexture())));
            FormAPI.create(player, subForm, () -> {
                if(subForm.wasClosed()) {
                    FormAPI.createLast(player, lastWindow);
                    return;
                }

                final HeadEntry headEntry = headEntries.get(subForm.getResponse().getClickedButtonId());
                if(headEntry == null) return;

                this.giveItem(player, headEntry);
            });
        }
    }

    public void giveItem(Player player, HeadEntry headEntry) {
        HeadAPI.resolveSkullOwner(headEntry.getTexture(), SkullOwnerResolveMethod.TEXTURE).whenComplete((skullOwner, throwable) -> {
            if(throwable != null) {
                player.sendMessage(PluginHolder.get().getLanguage().translate(player, de.kcodeyt.heads.lang.TranslationKey.ERROR_WHILE_GIVING_HEAD));
                throwable.printStackTrace();
                return;
            }

            final Item item = HeadAPI.createSkullItemByOwner(skullOwner);
            item.setCustomName("§r§7" + headEntry.getName());
            final Item[] drops = player.getInventory().addItem(item);
            if(drops.length > 0) {
                for(final Item drop : drops)
                    player.getLevel().dropItem(player, drop);
            }

            player.sendMessage(PluginHolder.get().getLanguage().translate(player, de.kcodeyt.heads.lang.TranslationKey.HEAD_GIVEN, headEntry.getName()));
        });
    }

}
