package cn.cookiestudio.gun.guns;

import cn.cookiestudio.gun.CoolDownTimer;
import cn.cookiestudio.gun.GunPlugin;
import cn.cookiestudio.gun.playersetting.PlayerSettingMap;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.entity.EntityInteractEvent;
import cn.nukkit.event.entity.ItemSpawnEvent;
import cn.nukkit.event.player.PlayerAnimationEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustomEdible;
import cn.nukkit.item.food.Food;
import cn.nukkit.item.food.FoodNormal;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.potion.Effect;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static cn.cookiestudio.gun.GunPlugin.plugin;
import static cn.cookiestudio.gun.utils.taskUtil.Async;

@Setter
@Getter
public abstract class ItemGunBase extends ItemCustomEdible {


    static {
        Server.getInstance().getPluginManager().registerEvents(new Listener(), GunPlugin.getInstance());
        Server.getInstance().getScheduler().scheduleRepeatingTask(GunPlugin.getInstance(), () -> Server.getInstance().getOnlinePlayers().values().forEach(player -> {
            if (player.getInventory().getItemInHand() instanceof ItemGunBase itemGun) {
                if (player.isSneaking()) {
                    itemGun.getGunData().addAimingSlownessEffect(player);
                } else {
                    itemGun.getGunData().addWalkingSlownessEffect(player);
                }
                if (!GunPlugin.getInstance().getCoolDownTimer().isCooling(player) || GunPlugin.getInstance().getCoolDownTimer().getCoolDownMap().get(player).getType() != CoolDownTimer.Type.RELOAD) {
                    if (GunPlugin.getInstance().getPlayerSettingPool().getPlayerSetting(player.getName()).getFireMode() == PlayerSettingMap.FireMode.AUTO) {
                        if (!GunPlugin.getInstance().getFireTask().firing(player)) {
                            player.sendActionBar("<" + itemGun.getAmmoCount() + "§f/" + itemGun.getGunData().getMagSize() + ">\n§e 单发");
                        } else {
                            player.sendActionBar("<" + itemGun.getAmmoCount() + "§f/" + itemGun.getGunData().getMagSize() + ">\n§c自动开火");
                        }
                    } else {
                        player.sendActionBar("<" + itemGun.getAmmoCount() + "§f/" + itemGun.getGunData().getMagSize() + ">");
                    }
                    return;
                }
                CoolDownTimer.CoolDown coolDown = GunPlugin.getInstance().getCoolDownTimer().getCoolDownMap().get(player);
                if (coolDown.getType() == CoolDownTimer.Type.RELOAD) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("RELOAD: §a");
                    int bound = (int) (30.0 * ((double) coolDown.coolDownTick / (itemGun.getGunData().getReloadTime() * 20)));
                    for (int i = 30; i >= 1; i--) {
                        if (i < bound) stringBuilder.append("|");
                        if (i == bound) stringBuilder.append("|§c");
                        if (i > bound) stringBuilder.append("|");
                    }
                    player.sendActionBar(stringBuilder.toString(), 0, 1, 0);
                }
            }
        }), 1);
    }

    protected GunData gunData;

    public ItemGunBase(String name) {
        super("gun:" + name, name, name);
    }

    public static GunData getGunData(Class<? extends ItemGunBase> clazz) {
        return GunPlugin.getInstance().getGunDataMap().get(clazz);
    }

    public abstract int getSkinId();

    public abstract float getDropItemScale();

    @Override
    public boolean canAlwaysEat() {
        return true;
    }

    public Map.Entry<Plugin, Food> getFood() {
        FoodNormal food = new FoodNormal(0, 0.0F);
        food.addRelative(this.getNamespaceId(), 0, plugin);

        food.setEatingTickSupplier(() -> (int) (this.gunData.getFireCoolDown() * 20.0));
        return Map.entry(plugin, food);
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        return false;
    }

    @Override
    public boolean onUse(Player player, int ticksUsed) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition
			.edibleBuilder(this, CreativeItemCategory.EQUIPMENT)
                .creativeGroup("itemGroup.name.gun")
                .allowOffHand(true)
                .build();
    }

    public GunInteractAction interact(Player player) {
        if (GunPlugin.getInstance().getCoolDownTimer().isCooling(player)) {
            return ItemGunBase.GunInteractAction.COOLING;
        } else {
            ItemGunBase itemGun = (ItemGunBase) player.getInventory().getItemInHand();
            if (itemGun.getAmmoCount() > 0) {
                itemGun.getGunData().fire(player, itemGun);
                if (player.getGamemode() != 1) {
                    itemGun.setAmmoCount(itemGun.getAmmoCount() - 1);
                }

                player.getInventory().setItem(player.getInventory().getHeldItemIndex(), itemGun);
                GunPlugin.getInstance().getCoolDownTimer().addCoolDown(player, (int) (itemGun.getGunData().getFireCoolDown() * 20.0), () -> {
                }, () -> CoolDownTimer.Operator.NO_ACTION, CoolDownTimer.Type.FIRECOOLDOWN);
                return ItemGunBase.GunInteractAction.FIRE;
            } else if (itemGun.getAmmoCount() == 0) {
                return itemGun.reload(player) ? ItemGunBase.GunInteractAction.RELOAD : ItemGunBase.GunInteractAction.EMPTY_GUN;
            } else {
                return null;
            }
        }
    }

    public GunInteractAction interact(EntityHuman entityHuman) {
        if (GunPlugin.getInstance().getCoolDownTimer().isCooling(entityHuman)) {
            return ItemGunBase.GunInteractAction.COOLING;
        } else {
            ItemGunBase itemGun = (ItemGunBase) entityHuman.getInventory().getItemInHand();
            if (itemGun.getAmmoCount() > 0) {
                itemGun.getGunData().fire(entityHuman, itemGun);
                itemGun.setAmmoCount(itemGun.getAmmoCount() - 1);
                entityHuman.getInventory().setItem(entityHuman.getInventory().getHeldItemIndex(), itemGun);
                GunPlugin.getInstance().getCoolDownTimer().addCoolDown(entityHuman, (int) (itemGun.getGunData().getFireCoolDown() * 20.0), () -> {
                }, () -> CoolDownTimer.Operator.NO_ACTION, CoolDownTimer.Type.FIRECOOLDOWN);
                return ItemGunBase.GunInteractAction.FIRE;
            } else if (itemGun.getAmmoCount() == 0) {
                return itemGun.reload(entityHuman) ? ItemGunBase.GunInteractAction.RELOAD : ItemGunBase.GunInteractAction.EMPTY_GUN;
            } else {
                return null;
            }
        }
    }

    public boolean reload(Player player) {
        CoolDownTimer coolDownTimer = GunPlugin.getInstance().getCoolDownTimer();
        if (coolDownTimer.isCooling(player)) {
            CoolDownTimer.CoolDown coolDown = coolDownTimer.getCoolDownMap().get(player);
            if (coolDown.getType() == CoolDownTimer.Type.RELOAD)
                coolDownTimer.interrupt(player);
            return false;
        }
        if (player.getGamemode() != Player.CREATIVE && !player.getInventory().contains(Item.fromString("gun:" + this.getGunData().getMagName()))) {
            this.getGunData().emptyGun(player);
            return false;
        }
        this.getGunData().startReload(player);
        GunPlugin.getInstance().getCoolDownTimer().addCoolDown(player, (int) (this.getGunData().getReloadTime() * 20), () -> {
            this.getGunData().reloadFinish(player);
            this.setAmmoCount(this.getGunData().getMagSize());
            if (player.getInventory() != null) {
                player.getInventory().setItem(player.getInventory().getHeldItemIndex(), this);
                if (player.getGamemode() != Player.CREATIVE) {
                    for (Map.Entry<Integer, Item> entry : player.getInventory().getContents().entrySet()) {
                        Item item = entry.getValue();
                        int slot = entry.getKey();
                        if (item.equals(Item.fromString("gun:" + this.getGunData().getMagName()))) {//todo:debug
                            item.setCount(item.count - 1);
                            player.getInventory().setItem(slot, item);
                            break;
                        }
                    }
                }
            }
        }, () -> {
            player.sendActionBar("§c换弹终止");
            return CoolDownTimer.Operator.INTERRUPT;
        }, CoolDownTimer.Type.RELOAD);
        return true;
    }

    public boolean reload(EntityHuman entityHuman) {
        CoolDownTimer coolDownTimer = GunPlugin.getInstance().getCoolDownTimer();
        if (coolDownTimer.isCooling(entityHuman)) {
            CoolDownTimer.CoolDown coolDown = coolDownTimer.getCoolDownMap().get(entityHuman);
            if (coolDown.getType() == CoolDownTimer.Type.RELOAD)
                coolDownTimer.interrupt(entityHuman);
            return false;
        }
        this.getGunData().startReload(entityHuman);
        GunPlugin.getInstance().getCoolDownTimer().addCoolDown(entityHuman, (int) (this.getGunData().getReloadTime() * 20), () -> {
            this.getGunData().reloadFinish(entityHuman);
            this.setAmmoCount(this.getGunData().getMagSize());
            entityHuman.getInventory().setItem(entityHuman.getInventory().getHeldItemIndex(), this);
        }, () -> CoolDownTimer.Operator.INTERRUPT, CoolDownTimer.Type.RELOAD);
        return true;
    }

    public int getAmmoCount() {
        if (this.getNamedTag() != null) {
            return this.getNamedTag().getInt("ammoCount");
        }
        return 0;
    }

    public void setAmmoCount(int count) {
        if (this.getNamedTag() != null) {
            this.setNamedTag(this.getNamedTag().putInt("ammoCount", count));
        } else {
            this.setNamedTag(new CompoundTag().putInt("ammoCount", count));
        }
    }

    public abstract ItemMagBase getItemMagObject();

    public enum GunInteractAction {
        FIRE,
        RELOAD,
        COOLING,
        EMPTY_GUN
    }

    private static class Listener implements cn.nukkit.event.Listener {
        // 玩家动画事件处理器（用于检测玩家手臂摆动动作）
        @EventHandler
        public void onPlayerAnimation(PlayerAnimationEvent event) {
            // 使用异步方法处理，避免阻塞主线程
            Async(() -> {
                Player p = event.getPlayer();
                if (event.getAnimationType() == AnimatePacket.Action.SWING_ARM && p.getInventory().getItemInHand() instanceof ItemGunBase) {
                    ItemGunBase itemGun;
                    Item var4;
                    if (GunPlugin.getInstance().getPlayerSettingPool().getSettings().get(p.getName()).getFireMode() != PlayerSettingMap.FireMode.AUTO) {
                        var4 = p.getInventory().getItemInHand();
                        if (var4 instanceof ItemGunBase) {
                            itemGun = (ItemGunBase) var4;
                            if (p.isSneaking()) {
                                itemGun.interact(p);
                            } else {
                                itemGun.reload(p);
                            }
                        }
                    } else {
                        var4 = p.getInventory().getItemInHand();
                        if (var4 instanceof ItemGunBase) {
                            itemGun = (ItemGunBase) var4;
                            itemGun.interact(p);
                        }
                    }
                }

            });
        }

        // 物品生成事件处理器（最高优先级，且忽略已取消的事件）
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onItemSpawn(ItemSpawnEvent e) {
            // 使用异步方法处理，避免阻塞主线程
            Async(() -> {
                // 获取生成的物品实体和物品堆栈
                Item item = e.getEntity().getItem();
                EntityItem drop = e.getEntity();

                // 如果已经是自定义物品实体，则直接返回不做处理
                if (drop instanceof EntityCustomItem) {
                    return;
                }

                // 处理枪械物品
                if (item instanceof ItemGunBase gun) {
                    // 创建自定义枪械掉落物实体
                    EntityCustomItem customDrop = new EntityCustomItem(
                            drop.getChunk(),       // 使用原掉落物的区块位置
                            drop.namedTag,        // 继承原物品的NBT数据
                            gun.getSkinId(),      // 获取枪械的皮肤ID
                            gun.getDropItemScale() // 获取枪械的掉落物缩放比例
                    );

                    // 移除原版掉落物实体（使用kill()避免NPE）
                    drop.kill();

                    // 将自定义掉落物显示给所有玩家
                    customDrop.spawnToAll();
                }
                // 处理弹匣物品
                else if (item instanceof ItemMagBase mag) {
                    // 创建自定义弹匣掉落物实体
                    EntityCustomItem customDrop = new EntityCustomItem(
                            drop.getChunk(),      // 使用原掉落物的区块位置
                            drop.namedTag,        // 继承原物品的NBT数据
                            mag.getSkinId(),      // 获取弹匣的皮肤ID
                            mag.getDropItemScale() // 获取弹匣的掉落物缩放比例
                    );

                    // 移除原版掉落物实体
                    drop.kill();

                    // 将自定义掉落物显示给所有玩家
                    customDrop.spawnToAll();
                }
            });
        }

        // 玩家交互事件处理器
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            // 使用异步方法处理，避免阻塞主线程
            Async(() -> {
                Player p = event.getPlayer();
                if (p == null || !p.isOnline()) return;
                if (p.getInventory().getItemInHand() instanceof ItemGunBase && (event.getAction() == cn.nukkit.event.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.getAction() == cn.nukkit.event.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
                    int playerfire = GunPlugin.playerfire.get(p);
                    if (playerfire > 6) {
                        GunPlugin.playerfire.put(p, 6);
                    } else {
                        GunPlugin.playerfire.put(p, GunPlugin.playerfire.get(p) + 4);
                    }

                    if (GunPlugin.playerFireNeedWaitTime.get(p) == null) {
                        GunPlugin.getInstance().getFireTask().fire(p);
                    }
                }

            });
        }

        // 实体交互事件处理器
        @EventHandler
        public void onEntityInteract(EntityInteractEvent event) {
            // 使用异步方法处理，避免阻塞主线程
            Async(() -> {
                Entity var3 = event.getEntity();
                if (var3 instanceof EntityHuman human) {
                    Item var4 = human.getInventory().getItemInHand();
                    if (var4 instanceof ItemGunBase) {
                        Player p = (Player) human;
                        int playerfire = GunPlugin.playerfire.get(p);
                        if (playerfire > 6) {
                            GunPlugin.playerfire.put(p, 6);
                        } else {
                            GunPlugin.playerfire.put(p, GunPlugin.playerfire.get(p) + 4);
                        }

                        if (GunPlugin.playerFireNeedWaitTime.get(p) == null) {
                            GunPlugin.getInstance().getFireTask().fire(p);
                        }
                    }
                }
            });
        }

        // 玩家切换手持物品事件处理器
        @EventHandler
        public void onPlayerHeldItem(PlayerItemHeldEvent event) {
            // 使用异步方法处理，避免阻塞主线程
            Async(() -> {
                // 检查玩家新切换到的物品是否是枪械基类ItemGunBase的实例
                if (!(event.getItem() instanceof ItemGunBase)) {
                    // 如果不是枪械，则移除玩家的减速效果(SLOWNESS)
                    event.getPlayer().removeEffect(Effect.SLOWNESS);
                }
            });
        }
    }

}
