package de.phyrone.lobbyrel.scheduler;

import de.phyrone.lobbyrel.LobbyPlugin;
import de.phyrone.lobbyrel.events.LobbyReloadEvent;
import de.phyrone.lobbyrel.lib.Tools;
import de.phyrone.lobbyrel.lib.json.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class StayTitleManager implements Listener {
    static StayTitleManager instance;

    private HashMap<Player, IHandledTitle> titles = new HashMap<>();
    private HashMap<Player, StaticTitleApi> apis = new HashMap<>();

    public StayTitleManager() {
        instance = this;

    }

    public static StayTitleManager getInstance() {
        return instance;
    }

    private IHandledTitle getIhandled(Player player) {
        if (!titles.containsKey(player))
            titles.put(player, new IHandledTitle(player));
        return titles.get(player);
    }

    public StaticTitleApi getTitle(Player player) {
        if (!apis.containsKey(player))
            apis.put(player, new StaticTitleApi() {
                @Override
                public void setTitle(TextHandler handler) {
                    getIhandled(player).handler = handler;
                }

                @Override
                public void setTitle(String title) {
                    getIhandled(player).setText(title);
                }

                @Override
                public boolean getEnabled() {
                    return getIhandled(player).enabled;
                }

                @Override
                public void setEnabled(boolean enabled) {
                    getIhandled(player).enabled = enabled;
                    getIhandled(player).updateSched();
                }

                @Override
                public String getText() {
                    return getIhandled(player).getText();
                }

                @Override
                public String getDynamicText() {
                    return getIhandled(player).getText(player);
                }

                @Override
                public Player getPlayer() {
                    return player;
                }
            });
        return apis.get(player);

    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent e) {
        remove(e.getPlayer());

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onReload(LobbyReloadEvent e) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            remove(player);
            add(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent e) {
        add(e.getPlayer());
    }

    private void remove(Player player) {
        if (apis.containsKey(player)) apis.remove(player);
        if (titles.containsKey(player)) {
            getTitle(player).setEnabled(false);
            titles.remove(player);
        }
    }

    private void add(Player player) {
        titles.put(player, new IHandledTitle(player));
    }

    public interface TextHandler {
        String onText(Player player);
    }

    public interface StaticTitleApi {
        void setTitle(TextHandler handler);

        void setTitle(String title);

        boolean getEnabled();

        void setEnabled(boolean enabled);

        String getText();

        String getDynamicText();

        Player getPlayer();
    }

}

class IHandledTitle {
    static BukkitScheduler s = Bukkit.getScheduler();
    Player player;
    StayTitleManager.TextHandler handler = null;
    String text = "&k-------";
    boolean enabled = false;
    BukkitTask task = null;
    Runnable runnable = () -> updateTitle();

    public IHandledTitle(Player player) {
        this.player = player;
    }

    private void updateTitle() {
        if (LobbyPlugin.getDebug())
            System.out.println("Send Title");
        String ctext = getText(player);
        if (ctext == null) {
            enabled = false;
            updateSched();
        }
        ctext = ChatColor.translateAlternateColorCodes('&', ctext);
        String[] lines = ctext.contains("\n") ? ctext.split("\n", 2) : new String[]{ctext};
        Tools.sendTitle(player, new FancyMessage(!lines[0].equals("") ? lines[0] : " "), 0, 40, 0);
        if (lines.length > 1)
            Tools.sendTitle(player, new FancyMessage(!lines[1].equals("") ? lines[1] : " "), true, 0, 40, 0);
    }

    public void updateSched() {
        if (enabled && !isRunning()) task = s.runTaskTimerAsynchronously(LobbyPlugin.getInstance(), runnable, 1, 5);
        else if (!enabled && isRunning()) {
            task.cancel();
            task = null;
            player.resetTitle();
        }

    }

    boolean isRunning() {
        if (task == null)
            return false;
        else return s.isCurrentlyRunning(task.getTaskId());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        handler = null;
        if (enabled)
            updateTitle();
    }

    public StayTitleManager.TextHandler getHandler() {
        return handler;
    }

    public String getText(Player player) {
        return handler != null ? handler.onText(player) : text;
    }
}
