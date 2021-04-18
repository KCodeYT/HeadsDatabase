package de.kcodeyt.headsdb.database;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import de.kcodeyt.heads.Heads;
import de.kcodeyt.heads.util.HeadInput;
import de.kcodeyt.headsdb.util.FormAPI;
import de.kcodeyt.headsdb.util.HeadRender;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Database {

    private static final String API_URL = "https://minecraft-heads.com/scripts/api.php";
    private static final Gson GSON = new Gson();

    private final Map<String, Integer> pageCount;
    private final List<Category> categories;
    private final List<HeadEntry> headEntries;

    public Database() {
        this.pageCount = new HashMap<>();
        this.categories = new ArrayList<>();
        this.headEntries = new ArrayList<>();
    }

    public boolean reload() {
        this.categories.clear();
        this.headEntries.clear();
        return this.load();
    }

    public boolean load() {
        try {
            for(final CategoryEnum category : CategoryEnum.values()) {
                final HttpURLConnection connection = (HttpURLConnection) new URL(API_URL + "?cat=" + category.getIdentifier()).openConnection();
                connection.setRequestProperty("User-Agent", "Chrome");
                connection.connect();

                if(connection.getResponseCode() == 200) {
                    try(final InputStream inputStream = connection.getInputStream();
                        final Reader reader = new InputStreamReader(inputStream)) {
                        final List<Map<String, String>> values = GSON.<List<Map<String, String>>>fromJson(reader, List.class);
                        final List<HeadEntry> headEntries = values.stream().map(map -> new HeadEntry(map.get("name"), map.get("uuid"), map.get("value"))).collect(Collectors.toList());
                        this.headEntries.addAll(headEntries);
                        this.categories.add(new Category(category, category.getDisplayName(), Iterables.getLast(headEntries).getTexture(), Collections.unmodifiableList(headEntries)));
                    }
                }

                connection.disconnect();
            }

            return true;
        } catch(IOException e) {
            Server.getInstance().getLogger().error("Error whilst loading heads from database api!", e);
            e.printStackTrace();
            return false;
        }
    }

    private List<List<HeadEntry>> toPages(Category category, int count) {
        final List<HeadEntry> headEntries = category.getEntries();
        final int dbSize = headEntries.size();
        if(dbSize <= 0)
            return Collections.emptyList();
        final List<List<HeadEntry>> pages = new ArrayList<>();
        final int chunks = (dbSize - 1) / count;
        for(int i = 0; i <= chunks; i++)
            pages.add(headEntries.subList(i * count, i == chunks ? dbSize : (i + 1) * count));
        return Collections.unmodifiableList(pages);
    }

    public void showForm(Player player) {
        final FormWindowSimple categoriesForm = new FormWindowSimple("Select a category", "");
        final List<Category> categories = Collections.unmodifiableList(new ArrayList<>(this.categories));
        for(final Category category : categories)
            categoriesForm.addButton(new ElementButton(category.getDisplayName(), HeadRender.createButtonImage(category.getDisplaySkin())));
        FormAPI.create(player, categoriesForm, () -> {
            if(categoriesForm.wasClosed())
                return;
            final Category category = categories.get(categoriesForm.getResponse().getClickedButtonId());
            if(category == null)
                return;
            final FormWindowSimple pagesForm = new FormWindowSimple("Select a page", "");
            final List<List<HeadEntry>> pages = this.toPages(category, this.pageCount.getOrDefault(player.getName(), 40));
            for(int i = 0; i < pages.size(); i++)
                pagesForm.addButton(new ElementButton("Page " + (i + 1), HeadRender.createButtonImage(Iterables.getLast(pages.get(i)).getTexture())));
            FormAPI.create(player, pagesForm, () -> {
                if(pagesForm.wasClosed()) {
                    this.showForm(player);
                    return;
                }

                final List<HeadEntry> headEntries = pages.get(pagesForm.getResponse().getClickedButtonId());
                if(headEntries == null)
                    return;
                final FormWindowSimple subForm = new FormWindowSimple(category.getDisplayName(), "");
                for(final HeadEntry headEntry : headEntries)
                    subForm.addButton(new ElementButton(headEntry.getName(), HeadRender.createButtonImage(headEntry.getTexture())));
                FormAPI.create(player, subForm, () -> {
                    if(subForm.wasClosed())
                        return;
                    final HeadEntry headEntry = headEntries.get(subForm.getResponse().getClickedButtonId());
                    if(headEntry == null)
                        return;
                    this.giveItem(player, headEntry);
                });
            });
        });
    }

    public void giveItem(Player player, HeadEntry headEntry) {
        Heads.createItem(HeadInput.ofTexture(headEntry.getTexture(), headEntry.getId())).whenComplete((result, throwable) -> {
            if(throwable != null) {
                player.sendMessage("§cCould not create the requested skull item!");
                throwable.printStackTrace();
                return;
            }

            final Item item = result.getItem();
            item.setCustomName("§r§7" + headEntry.getName());
            final Item[] drops = player.getInventory().addItem(item);
            if(drops.length > 0) {
                for(final Item drop : drops)
                    player.getLevel().dropItem(player, drop);
            }

            player.sendMessage("§aGave you the head " + headEntry.getName() + "§r§a!");
        });
    }

}
