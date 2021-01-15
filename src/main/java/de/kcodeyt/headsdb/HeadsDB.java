package de.kcodeyt.headsdb;

import cn.nukkit.plugin.PluginBase;
import de.kcodeyt.headsdb.command.HeadDBCommand;
import de.kcodeyt.headsdb.database.Database;
import lombok.Getter;

@Getter
public class HeadsDB extends PluginBase {

    private final Database database = new Database();

    @Override
    public void onEnable() {
        this.getServer().getCommandMap().register("headsdb", new HeadDBCommand(this));
        if(!this.database.load()) {
            this.getLogger().error("Could not load heads database!");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {

    }

}
