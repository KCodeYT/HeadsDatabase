package de.kcodeyt.headsdb.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.window.FormWindowCustom;
import de.kcodeyt.headsdb.HeadsDB;
import de.kcodeyt.headsdb.database.DBSkin;
import de.kcodeyt.headsdb.util.FormAPI;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class HeadDBCommand extends Command {

    private final HeadsDB headsDB;

    public HeadDBCommand(HeadsDB headsDB) {
        super("hdb");
        this.setAliases(new String[]{"headsdb"});
        this.headsDB = headsDB;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(args.length > 0) {
            final String subCommand = args[0].toLowerCase();
            switch(subCommand) {
                case "reload":
                    if(sender.isOp()) {
                        if(this.headsDB.getDatabase().reload())
                            sender.sendMessage("§aReload the heads database successfully!");
                        else
                            sender.sendMessage("§cCould not reload the database!");
                        return true;
                    }
                    break;
                case "random":
                    if(sender.isOp()) {
                        if(this.isConsole(sender))
                            return false;

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

                        final List<DBSkin> dbSkins = this.headsDB.getDatabase().getDbSkins();
                        final Random random = ThreadLocalRandom.current();
                        for(int i = 0; i < amount; i++)
                            this.headsDB.getDatabase().giveItem((Player) sender, dbSkins.get(random.nextInt(dbSkins.size())));
                        return true;
                    }
                    break;
                case "config":
                    if(this.isConsole(sender))
                        return false;

                    final Player player = (Player) sender;
                    final FormWindowCustom configForm = new FormWindowCustom("Configure");
                    configForm.addElement(new ElementSlider("Page length", 20, 120, 5, 40));
                    FormAPI.create(player, configForm, () -> {
                        if(configForm.wasClosed())
                            return;
                        final int pageLength = (int) configForm.getResponse().getSliderResponse(0);
                        this.headsDB.getDatabase().getPageCount().put(player.getName(), pageLength);
                        player.sendMessage("§aSet your page length to " + pageLength + "!");
                    });
                    return true;
            }
        }

        if(this.isConsole(sender))
            return false;

        this.headsDB.getDatabase().showForm((Player) sender);
        return true;
    }

    private boolean isConsole(CommandSender sender) {
        if(sender instanceof Player)
            return false;
        sender.sendMessage("You must be logged in, to be able to use this command!");
        return true;
    }

}
