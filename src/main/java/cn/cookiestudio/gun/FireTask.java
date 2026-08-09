package cn.cookiestudio.gun;

import cn.cookiestudio.gun.guns.ItemGunBase;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.PluginTask;
import lombok.Getter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Getter
public class FireTask extends PluginTask {
    private final Map<Player, Boolean> firing = new HashMap<>();

    public FireTask(Plugin owner) {
        super(owner);
        Server.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                firing.remove(event.getPlayer());
            }

            @EventHandler
            public void onPlayerInteractFiring(PlayerItemHeldEvent event) {
                if (firing.containsKey(event.getPlayer()) && !(event.getItem() instanceof ItemGunBase)) {
                    firing.put(event.getPlayer(), false);
                }

            }
        }, GunPlugin.getInstance());
        Server.getInstance().getScheduler().scheduleRepeatingTask(this, 1, true);
    }

    public void onRun(int i) {
        Iterator<Player> var2 = Server.getInstance().getOnlinePlayers().values().iterator();

        while(var2.hasNext()) {
            Player p = var2.next();
            if (GunPlugin.playerFireNeedWaitTime.get(p) != null && GunPlugin.playerFireNeedWaitTime.get(p) > 0) {
                GunPlugin.playerFireNeedWaitTime.put(p, GunPlugin.playerFireNeedWaitTime.get(p) - 1);
            }

            if (GunPlugin.playerfire.get(p) == null) {
                GunPlugin.playerfire.put(p, 0);
            } else if (GunPlugin.playerfire.get(p) > 0) {
                GunPlugin.playerfire.put(p, GunPlugin.playerfire.get(p) - 1);
            } else {
                this.firing.put(p, false);
            }
        }

        this.firing.keySet().forEach((player) -> {
            if (this.firing.get(player)) {
                Item patt2156$temp = player.getInventory().getItemInHand();
                if (patt2156$temp instanceof ItemGunBase itemGun) {
                    ItemGunBase.GunInteractAction var4 = itemGun.interact(player);
                    if (var4 == ItemGunBase.GunInteractAction.RELOAD) {
                        this.firing.put(player, false);
                    }
                } else {
                    this.firing.put(player, false);
                }

            }
        });
    }

    public void fire(Player player) {
        if (GunPlugin.playerfire.get(player) > 0) {
            this.firing.put(player, true);
        }
    }
    public void fire2(Player player) {

    }

    public void changeState(Player player) {
        if (!this.firing.containsKey(player)) {
            this.firing.put(player, true);
        }
        if (this.firing.get(player)) {
            this.firing.put(player, false);
        } else {
            this.firing.put(player, true);
        }

    }

    public boolean firing(Player player) {
        if (!this.firing.containsKey(player)) {
            this.firing.put(player, false);
        }

        return this.firing.get(player);
    }

}
