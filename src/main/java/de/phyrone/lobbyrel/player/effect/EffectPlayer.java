package de.phyrone.lobbyrel.player.effect;

import de.phyrone.lobbyrel.LobbyPlugin;
import de.phyrone.lobbyrel.config.Config;
import de.phyrone.lobbyrel.lib.Sounds;
import de.phyrone.lobbyrel.player.data.OfflinePlayerData;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.DnaEffect;
import de.slikey.effectlib.effect.FlameEffect;
import de.slikey.effectlib.effect.WarpEffect;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectPlayer {
    static EffectManager m = new EffectManager(LobbyPlugin.getInstance());
    Player p;

    public EffectPlayer(Player player) {
        this.p = player;
    }

    private Boolean allowSound() {
        return new OfflinePlayerData(p).getSound();
    }

    public void teleportEffect() {
        Bukkit.getScheduler().runTaskAsynchronously(LobbyPlugin.getInstance(), () -> {
            playSound(Sounds.ENDERMAN_TELEPORT, 1, 1);
            DnaEffect e = new DnaEffect(m);
            e.asynchronous = true;
            Location loc = p.getLocation();
            loc.setPitch(-90);
            loc.setYaw(0);
            e.length = 10;
            e.duration = 1000;
            e.setLocation(loc);
            e.start();
        });
    }

    public void changeHider() {
        p.closeInventory();
        playSound(Sounds.PORTAL_TRAVEL, 0.3F, 1.7F);
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 25, 10, false, false), true);

    }

    public void blocked() {
        if (allowSound()) {
            playSound(Sounds.BURP, 0.3F, 2);
        }
    }

    public void JumpPad() {
        Bukkit.getScheduler().runTaskAsynchronously(LobbyPlugin.getInstance(), () -> {
            FlameEffect ef = new FlameEffect(m);
            ef.asynchronous = true;
            ef.duration = 50;
            ef.setLocation(p.getLocation());
            ef.start();
            if (allowSound()) {
                p.playSound(p.getLocation(), Config.getString("Sound.JumpPad", "FIZZ"), 0.5F, 1);
            }
        });
    }

    public void playSound(Location location, Sounds sound, float volume, float pitch) {
        Bukkit.getScheduler().runTaskAsynchronously(LobbyPlugin.getInstance(), () -> {
            if (allowSound())
                p.playSound(location, sound.bukkitSound(), volume, pitch);

        });
    }

    public void playSound(Sounds sound, float volume, float pitch) {
        playSound(p.getLocation(), sound, volume, pitch);
    }

    public void dJump() {
        Bukkit.getScheduler().runTaskAsynchronously(LobbyPlugin.getInstance(), () -> {
            WarpEffect ef = new WarpEffect(m);
            ef.asynchronous = true;
            ef.duration = 50;
            ef.color = Color.PURPLE;
            ef.setLocation(p.getLocation());
            ef.start();
            try {
                playSound(Sounds.ENDERDRAGON_WINGS, 0.5F, 1);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        });
    }

}
