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

package de.kcodeyt.headsdb;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import de.kcodeyt.headsdb.command.HeadDBCommand;
import de.kcodeyt.headsdb.database.Database;
import de.kcodeyt.headsdb.lang.Language;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Getter
public class HeadsDB extends PluginBase {

    private static final String DEFAULT_LANGUAGE = "en_US";

    public static final String MHF_QUESTION_TEXTURE_ID = "d34e063cafb467a5c8de43ec78619399f369f4a52434da8017a983cdd92516a0";

    private final Database database = new Database(this);

    private Language language;

    @Override
    public void onEnable() {
        this.getServer().getCommandMap().register("headsdb", new HeadDBCommand(this));
        this.database.load().whenComplete((success, error) -> {
            if(!success || error != null) {
                this.getLogger().error("Could not load heads database!", error);
                this.getServer().getPluginManager().disablePlugin(this);
            } else {
                this.getLogger().info("Successfully load " + this.database.getHeadEntries().size() + " Heads!");
            }
        });

        this.saveResource("config.yml");
        final Config config = this.getConfig();

        if(!config.exists("default_lang")) {
            config.set("default_lang", DEFAULT_LANGUAGE);
            config.save();
        }

        final File langDir = new File(this.getDataFolder(), "lang");
        final File[] files = langDir.listFiles();
        if(!langDir.exists() || files == null || files.length == 0) {
            if(!langDir.exists() && !langDir.mkdirs()) {
                this.getLogger().error("Could not create the language directory for this plugin!");
                return;
            }

            try(final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getResource("lang/lang_list.txt")))) {
                String line;
                while((line = bufferedReader.readLine()) != null)
                    this.saveResource("lang/" + line + ".txt");
            } catch(Exception e) {
                this.getLogger().error("Could not find the language resources of this plugin!", e);
                return;
            }
        }

        try {
            final String defaultLang = config.getString("default_lang");

            this.language = new Language(langDir, defaultLang);
            this.getLogger().info("This plugin is using the " + this.language.getDefaultLang() + " as default language file!");
        } catch(IOException e) {
            this.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public void onDisable() {

    }

}
