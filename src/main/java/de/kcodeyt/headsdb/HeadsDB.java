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
import de.kcodeyt.headsdb.command.HeadDBCommand;
import de.kcodeyt.headsdb.database.Database;
import lombok.Getter;

@Getter
public class HeadsDB extends PluginBase {

    public static final String MHF_QUESTION_TEXTURE_ID = "d34e063cafb467a5c8de43ec78619399f369f4a52434da8017a983cdd92516a0";

    private final Database database = new Database();

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
    }

    @Override
    public void onDisable() {

    }

}
