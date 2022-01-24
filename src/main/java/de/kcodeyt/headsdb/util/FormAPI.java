package de.kcodeyt.headsdb.util;

import cn.nukkit.Player;
import cn.nukkit.Server;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FormAPI {

    private static final Map<FormWindow, Handler> HANDLERS = new HashMap<>();

    public static void create(Player player, FormWindow formWindow, Runnable onResponse) {
        new Handler(player, formWindow, onResponse);
    }

    public static void createLast(Player player, FormWindow formWindow) {
        if(HANDLERS.containsKey(formWindow))
            create(player, formWindow, HANDLERS.get(formWindow).runnable);
    }

    public static class FormListener implements Listener {

        private static final AtomicBoolean INITIATED = new AtomicBoolean(false);

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
            final Handler handler = HANDLERS.get(formWindow);
            if(handler != null)
                handler.handle(isQuit);
        }

    }

    private static class Handler {

        private final Runnable runnable;
        private final TaskHandler taskHandler;

        private Handler(Player player, FormWindow formWindow, Runnable runnable) {
            final Server server = player.getServer();
            this.runnable = runnable;
            this.taskHandler = server.getScheduler().scheduleDelayedRepeatingTask(null, player::sendExperience, 3, 3);

            player.showFormWindow(formWindow);
            HANDLERS.put(formWindow, this);
            if(!FormListener.INITIATED.get()) {
                FormListener.INITIATED.set(true);
                final PluginManager pluginManager = server.getPluginManager();
                Plugin found = null;
                for(final Plugin plugin : pluginManager.getPlugins().values()) {
                    if(plugin instanceof HeadsDB) {
                        found = plugin;
                        break;
                    }
                }

                if(found != null)
                    pluginManager.registerEvents(new FormListener(), found);
            }
        }

        private void handle(boolean isQuit) {
            this.taskHandler.cancel();
            if(!isQuit)
                this.runnable.run();
        }

    }

}
