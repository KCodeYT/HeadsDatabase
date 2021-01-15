package de.kcodeyt.headsdb.database;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Database {

    private static final String API_URL = "https://minecraft-heads.com/scripts/api.php";
    private static final Gson GSON = new Gson();

    private final Map<String, Integer> pageCount;
    private final List<Category> categories;
    private final List<DBSkin> dbSkins;

    public Database() {
        this.pageCount = new HashMap<>();
        this.categories = new ArrayList<>();
        this.dbSkins = new ArrayList<>();
    }

    public boolean reload() {
        this.categories.clear();
        this.dbSkins.clear();
        return this.load();
    }

    public boolean load() {
        try {
            for(final Categories value : Categories.values()) {
                final List<Map<String, String>> values = GSON.<List<Map<String, String>>>fromJson(this.httpRequest(API_URL + "?cat=" + value.getIdentifier()), List.class);
                final List<DBSkin> dbSkins = values.stream().map(map -> new DBSkin(map.get("name"), map.get("uuid"), map.get("value"))).collect(Collectors.toList());
                this.dbSkins.addAll(dbSkins);
                this.categories.add(new Category(value, value.getDisplayName(), Iterables.getLast(dbSkins).getTexture(), Collections.unmodifiableList(dbSkins)));
            }

            return true;
        } catch(IOException e) {
            Server.getInstance().getLogger().error("Error whilst loading heads from database api!", e);
            e.printStackTrace();
            return false;
        }
    }

    private String httpRequest(String urlSpec) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(urlSpec).openConnection();
        connection.setRequestProperty("User-Agent", "Chrome");

        String content = "{}";
        if(connection.getResponseCode() == 200) {
            try(final InputStream inputStream = connection.getInputStream()) {
                final StringBuilder builder = new StringBuilder();
                final byte[] bytes = new byte[1024 * 1024];
                for(int read; (read = inputStream.read(bytes)) > 0; )
                    builder.append(new String(Arrays.copyOf(bytes, read), StandardCharsets.UTF_8));
                content = builder.toString();
            }
        }

        connection.disconnect();
        return content;
    }

    private List<List<DBSkin>> toPages(Category category, int count) {
        final List<DBSkin> dbSkins = category.getSkins();
        final int dbSize = dbSkins.size();
        if(dbSize <= 0)
            return Collections.emptyList();
        final List<List<DBSkin>> pages = new ArrayList<>();
        final int chunks = (dbSize - 1) / count;
        for(int i = 0; i <= chunks; i++)
            pages.add(dbSkins.subList(i * count, i == chunks ? dbSize : (i + 1) * count));
        return Collections.unmodifiableList(pages);
    }

    public void showForm(Player player) {
        final FormWindowSimple categoriesForm = new FormWindowSimple("Select a category", "");
        final List<Category> categories = Collections.unmodifiableList(new ArrayList<>(this.categories));
        for(final Category category : categories)
            categoriesForm.addButton(new ElementButton(category.getDisplayName(), new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_URL, HeadRender.createUrl(category.getDisplaySkin()))));
        FormAPI.create(player, categoriesForm, () -> {
            if(categoriesForm.wasClosed())
                return;
            final Category category = categories.get(categoriesForm.getResponse().getClickedButtonId());
            if(category == null)
                return;
            final FormWindowSimple pagesForm = new FormWindowSimple("Select a page", "");
            final List<List<DBSkin>> pages = this.toPages(category, this.pageCount.getOrDefault(player.getName(), 40));
            for(int i = 0; i < pages.size(); i++)
                pagesForm.addButton(new ElementButton("Page " + (i + 1), new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_URL, HeadRender.createUrl(Iterables.getLast(pages.get(i)).getTexture()))));
            FormAPI.create(player, pagesForm, () -> {
                if(pagesForm.wasClosed()) {
                    this.showForm(player);
                    return;
                }

                final List<DBSkin> dbSkins = pages.get(pagesForm.getResponse().getClickedButtonId());
                if(dbSkins == null)
                    return;
                final FormWindowSimple subForm = new FormWindowSimple(category.getDisplayName(), "");
                for(final DBSkin dbSkin : dbSkins)
                    subForm.addButton(new ElementButton(dbSkin.getName(), new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_URL, HeadRender.createUrl(dbSkin.getTexture()))));
                FormAPI.create(player, subForm, () -> {
                    if(subForm.wasClosed())
                        return;
                    final DBSkin dbSkin = dbSkins.get(subForm.getResponse().getClickedButtonId());
                    if(dbSkin == null)
                        return;
                    this.giveItem(player, dbSkin);
                });
            });
        });
    }

    public void giveItem(Player player, DBSkin dbSkin) {
        Heads.createItem(HeadInput.ofTexture(dbSkin.getTexture(), dbSkin.getId())).whenComplete((result, throwable) -> {
            if(throwable != null) {
                player.sendMessage("§cCould not create the requested skull item!");
                return;
            }

            final Item item = result.getItem();
            item.setCustomName("§r§7" + dbSkin.getName());
            final Item[] drops = player.getInventory().addItem(item);
            if(drops.length > 0) {
                for(final Item drop : drops)
                    player.getLevel().dropItem(player, drop);
            }

            player.sendMessage("§aGave you the head " + dbSkin.getName() + "§r§a!");
        });
    }

}
