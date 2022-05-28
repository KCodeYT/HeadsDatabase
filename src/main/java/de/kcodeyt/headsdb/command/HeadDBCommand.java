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

package de.kcodeyt.headsdb.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.window.FormWindowCustom;
import de.kcodeyt.headsdb.HeadsDB;
import de.kcodeyt.headsdb.database.HeadEntry;
import de.kcodeyt.headsdb.lang.Language;
import de.kcodeyt.headsdb.lang.TranslationKey;
import de.kcodeyt.headsdb.util.FormAPI;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class HeadDBCommand extends Command {

    private final HeadsDB headsDB;

    public HeadDBCommand(HeadsDB headsDB) {
        super("hdb");
        this.setPermission("headsdb.command.hdb");
        this.setAliases(new String[]{"headsdb"});
        this.headsDB = headsDB;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(!this.testPermission(sender)) return false;

        final Language language = this.headsDB.getLanguage();

        if(args.length > 0) {
            final String subCommand = args[0].toLowerCase();
            switch(subCommand) {
                case "reload":
                    if(sender.isOp()) {
                        sender.sendMessage(language.translate(sender instanceof Player ? (Player) sender : null, TranslationKey.DATABASE_RELOAD_START));

                        try {
                            this.headsDB.getLanguage().reload();
                        } catch(IOException e) {
                            this.headsDB.getLogger().error("Error while reloading language file", e);
                            sender.sendMessage(language.translate(sender instanceof Player ? (Player) sender : null, TranslationKey.DATABASE_RELOAD_FAILED));
                            return true;
                        }

                        this.headsDB.getDatabase().reload().whenComplete((result, error) -> {
                            if(!result || error != null) {
                                this.headsDB.getLogger().error("Could not load heads database!", error);
                                sender.sendMessage(language.translate(sender instanceof Player ? (Player) sender : null, TranslationKey.DATABASE_RELOAD_FAILED));
                            } else {
                                sender.sendMessage(language.translate(sender instanceof Player ? (Player) sender : null, TranslationKey.DATABASE_RELOAD_SUCCESS));
                                this.headsDB.getLogger().info("Successfully load " + this.headsDB.getDatabase().getHeadEntries().size() + " Heads!");
                            }
                        });
                        return true;
                    }
                    break;
                case "random":
                    if(sender.isOp()) {
                        if(this.isConsole(sender)) return false;

                        int amount;
                        try {
                            amount = Integer.parseInt(args.length > 1 ? args[1] : "1");
                            if(amount > 32)
                                amount = 32;
                            if(amount < 1)
                                amount = 1;
                        } catch(NumberFormatException e) {
                            amount = 1;
                        }

                        final List<HeadEntry> headEntries = this.headsDB.getDatabase().getHeadEntries();
                        final Random random = ThreadLocalRandom.current();
                        for(int i = 0; i < amount; i++)
                            this.headsDB.getDatabase().giveItem((Player) sender, headEntries.get(random.nextInt(headEntries.size())));

                        sender.sendMessage(language.translate((Player) sender, TranslationKey.GAVE_RANDOM_HEADS, amount));
                        return true;
                    }
                    break;
                case "config":
                    if(this.isConsole(sender)) return false;

                    final Player player = (Player) sender;
                    final FormWindowCustom configForm = new FormWindowCustom(language.translate(player, TranslationKey.CONFIG_FORM_TITLE));
                    configForm.addElement(new ElementSlider(language.translate(player, TranslationKey.CONFIG_FORM_PAGE_SIZE), 20, 120, 5, 40));
                    FormAPI.create(player, configForm, () -> {
                        if(configForm.wasClosed()) return;

                        final int pageLength = (int) configForm.getResponse().getSliderResponse(0);
                        this.headsDB.getDatabase().getPageCount().put(player.getName(), pageLength);
                        player.sendMessage(language.translate(player, TranslationKey.CONFIG_FORM_PAGE_SIZE_CHANGED, pageLength));
                    });
                    return true;
            }
        }

        if(this.isConsole(sender)) return false;

        this.headsDB.getDatabase().showForm((Player) sender);
        return true;
    }

    private boolean isConsole(CommandSender sender) {
        if(sender instanceof Player) return false;
        sender.sendMessage(this.headsDB.getLanguage().translate(null, TranslationKey.CONSOLE_USES_PLAYER_COMMAND));
        return true;
    }

}
