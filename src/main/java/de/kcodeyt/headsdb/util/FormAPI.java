package de.kcodeyt.headsdb.util;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.TaskHandler;
import de.kcodeyt.headsdb.HeadsDB;

import java.lang.reflect.Field;
import java.util.*;

public class FormAPI {

    private static final Map<FormWindow, Handler> HANDLERS = new HashMap<>();
    private static final FormListener FORM_LISTENER = new FormListener();

    public static void create(Player player, FormWindow formWindow, Runnable onResponse) {
        new Handler(player, formWindow, onResponse);
    }

    public static class FormListener implements Listener {

        private HeadsDB headsDB;

        @EventHandler
        public void onForm(PlayerFormRespondedEvent event) {
            this.handle(event.getWindow(), false);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            final Player player = event.getPlayer();
            try {
                final Field formWindowsField = player.getClass().getDeclaredField("formWindows");
                formWindowsField.setAccessible(true);
                for(final Object formWindow : ((Map<?, ?>) formWindowsField.get(player)).values())
                    this.handle((FormWindow) formWindow, true);
            } catch(IllegalAccessException | NoSuchFieldException e) {
                player.getServer().getLogger().warning("Could not access Player.formWindows field", e);
            }
        }

        private void handle(FormWindow formWindow, boolean isQuit) {
            final Handler handler = HANDLERS.remove(formWindow);
            if(handler != null)
                handler.handle(isQuit);
        }

        private boolean register(Player player) {
            if(this.headsDB != null)
                return true;
            final PluginManager pluginManager = player.getServer().getPluginManager();
            for(final Plugin plugin : pluginManager.getPlugins().values()) {
                if(plugin instanceof HeadsDB) {
                    pluginManager.registerEvents(this, this.headsDB = (HeadsDB) plugin);
                    return true;
                }
            }
            return false;
        }

    }

    private static class Handler {

        private final Runnable runnable;
        private final TaskHandler taskHandler;

        private Handler(Player player, FormWindow formWindow, Runnable runnable) {
            this.runnable = runnable;
            if(FORM_LISTENER.register(player)) {
                HANDLERS.put(formWindow, this);
                player.showFormWindow(formWindow);
                this.taskHandler = player.getServer().getScheduler().scheduleDelayedRepeatingTask(FORM_LISTENER.headsDB, player::sendExperience, 3, 3);
                return;
            }

            this.taskHandler = null;
        }

        private void handle(boolean isQuit) {
            this.taskHandler.cancel();
            if(!isQuit)
                this.runnable.run();
        }

    }

}
