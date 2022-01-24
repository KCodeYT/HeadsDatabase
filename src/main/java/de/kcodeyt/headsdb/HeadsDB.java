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
