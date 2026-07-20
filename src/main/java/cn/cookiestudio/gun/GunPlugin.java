package cn.cookiestudio.gun;

import cn.cookiestudio.gun.command.GunCommand;
import cn.cookiestudio.gun.config.GunConfig;
import cn.cookiestudio.gun.config.GunConfig.GunSettings;
import cn.cookiestudio.gun.guns.EntityCustomItem;
import cn.cookiestudio.gun.guns.GunData;
import cn.cookiestudio.gun.guns.ItemGunBase;
import cn.cookiestudio.gun.guns.achieve.*;
import cn.cookiestudio.gun.playersetting.PlayerSettingPool;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Getter
public class GunPlugin extends PluginBase {
    public static Plugin plugin;
    public static Server nkServer;

    @Getter
    private static GunPlugin instance;
    private final Map<Class<? extends ItemGunBase>, GunData> gunDataMap = new HashMap<>();
    public static HashMap<Player, Integer> playerfire = new HashMap<>();
    public static HashMap<Player, Integer> playerFireNeedWaitTime = new HashMap<>();
    private final Map<String, Class<? extends ItemGunBase>> stringClassMap = new HashMap<>();
    private GunConfig gunConfig;
    private CoolDownTimer coolDownTimer;

    private PlayerSettingPool playerSettingPool;
    private FireTask fireTask;

    {
        stringClassMap.put("akm", ItemGunAkm.class);
        stringClassMap.put("awp", ItemGunAwp.class);
        stringClassMap.put("barrett", ItemGunBarrett.class);
        stringClassMap.put("m3", ItemGunM3.class);
        stringClassMap.put("m249", ItemGunM249.class);
        stringClassMap.put("mk18", ItemGunMk18.class);
        stringClassMap.put("mp5", ItemGunMp5.class);
        stringClassMap.put("p90", ItemGunP90.class);
        stringClassMap.put("taurus", ItemGunTaurus.class);
    }

    private static byte[] getBytes(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }
    @Override
    public void onLoad() {
        nkServer = getServer();
        plugin = this;
    }
    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        playerSettingPool = new PlayerSettingPool();
        fireTask = new FireTask(this);
        copyResource();
        loadGunData();
        registerEntity();
        registerListener();
        registerCommand();
        coolDownTimer = new CoolDownTimer();
    }

    private void registerEntity() {
        EntityManager.get().registerDefinition(EntityCustomItem.DEFINITION);
    }

    private void copyResource() {
        Path p = Paths.get(Server.getInstance().getDataPath() + "resource_packs/gun.zip");
        if (!Files.exists(p)) {
            this.getLogger().warning("未在目录" + p + "下找到材质包，正在复制，请在完成后重启服务器应用更改");
            try {
                Files.copy(this.getClass().getClassLoader().getResourceAsStream("resources/gun.zip"), p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        getDataFolder().mkdirs();
        gunConfig = ConfigManager.create(GunConfig.class, config -> {
            config.configure(options -> {
                options.configurer(new YamlSnakeYamlConfigurer());
                options.bindFile(configFile);
                options.removeOrphans(true);
            });
            config.saveDefaults();
            config.load(true);
        });
    }

    private void loadGunData() {
        gunConfig.guns().forEach((gunName, settings) -> {
            GunData gunData = GunData
                    .builder()
                    .gunName(gunName)
                    .magName(settings.magName())
                    .hitDamage(settings.hitDamage())
                    .fireCoolDown(settings.fireCoolDown())
                    .magSize(settings.magSize())
                    .slownessLevel(settings.slownessLevel())
                    .slownessLevelAim(settings.slownessLevelAim())
                    .particle(settings.particle())
                    .reloadTime(settings.reloadTime())
                    .range(settings.range())
                    .recoil(settings.recoil())
                    .fireSwingIntensity(settings.fireSwingIntensity())
                    .fireSwingDuration(settings.fireSwingDuration())
                    .build();
            Class<? extends ItemGunBase> gunClass = stringClassMap.get(gunName);
            gunDataMap.put(gunClass, gunData);
            try {
                ItemGunBase itemGun = gunClass.newInstance();
                Item.registerCustomItem(itemGun.getClass());
                Item.registerCustomItem(itemGun.getItemMagObject().getClass());
            } catch (InstantiationException | IllegalAccessException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void registerListener() {
        Server.getInstance().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onDataPacketReceive(DataPacketReceiveEvent event) {
                if (event.getPacket() instanceof EntityEventPacket packet) {
                    if (packet.event == EntityEventPacket.EATING_ITEM) {
                        Player player = event.getPlayer();
                        if (player.getInventory().getItemInHand() instanceof ItemGunBase) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }, this);
    }

    private void registerCommand() {
        Server.getInstance().getCommandMap().register("", new GunCommand("gun"));
    }

    public void saveGunData(GunData gunData) {
        GunSettings settings = gunConfig.guns().get(gunData.getGunName());
        if (settings == null) {
            throw new IllegalArgumentException("Unknown gun: " + gunData.getGunName());
        }
        settings.magSize(gunData.getMagSize());
        settings.fireCoolDown(gunData.getFireCoolDown());
        settings.reloadTime(gunData.getReloadTime());
        settings.slownessLevel(gunData.getSlownessLevel());
        settings.slownessLevelAim(gunData.getSlownessLevelAim());
        settings.fireSwingIntensity(gunData.getFireSwingIntensity());
        settings.fireSwingDuration(gunData.getFireSwingDuration());
        settings.hitDamage(gunData.getHitDamage());
        settings.range(gunData.getRange());
        settings.particle(gunData.getParticle());
        settings.magName(gunData.getMagName());
        settings.recoil(gunData.getRecoil());
        gunConfig.save();
    }

}
