package de.kcodeyt.headsdb.util;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.scheduler.TaskHandler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FormAPI {

    private static final Map<FormWindow, Handler> HANDLERS = new HashMap<>();

    public static void create(Player player, FormWindow formWindow, Runnable onResponse) {
        new Handler(player, formWindow, onResponse);
    }

    public static class FormListener implements Listener {

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

    }

    private static class Handler {

        private final Runnable runnable;
        private final TaskHandler taskHandler;

        private Handler(Player player, FormWindow formWindow, Runnable runnable) {
            this.runnable = runnable;
            player.showFormWindow(formWindow);
            this.taskHandler = player.getServer().getScheduler().scheduleDelayedRepeatingTask(null, player::sendExperience, 3, 3);
            HANDLERS.put(formWindow, this);
        }

        private void handle(boolean isQuit) {
            this.taskHandler.cancel();
            if(!isQuit)
                this.runnable.run();
        }

    }

}
