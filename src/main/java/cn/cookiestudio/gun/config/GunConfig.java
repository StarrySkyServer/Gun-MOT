package cn.cookiestudio.gun.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(fluent = true)
@Header("########################################")
@Header("Gun Configuration")
@Header("########################################")
public class GunConfig extends OkaeriConfig {

    @Comment("AKM settings")
    private GunSettings akm = new GunSettings(30, "akm_mag", 0.1, 4.0, 2, 4, 7.0, 300.0, 0.0, 0.15, 0.1);

    @Comment("MK18 settings")
    private GunSettings mk18 = new GunSettings(30, "mk18_mag", 0.08, 3.0, 1, 5, 5.0, 300.0, 0.0, 0.1, 0.1);

    @Comment("M249 settings")
    private GunSettings m249 = new GunSettings(100, "m249_mag", 0.06, 8.0, 3, 5, 2.5, 300.0, 0.0, 0.08, 0.1);

    @Comment("Taurus settings")
    private GunSettings taurus = new GunSettings(17, "taurus_mag", 0.2, 2.0, 1, 2, 4.0, 150.0, 0.0, 0.05, 0.1);

    @Comment("M3 settings")
    private GunSettings m3 = new GunSettings(8, "m3_ammo", 1.25, 4.0, 2, 3, 3.0, 75.0, 0.0, 0.3, 0.1);

    @Comment("MP5 settings")
    private GunSettings mp5 = new GunSettings(25, "mp5_mag", 0.075, 3.0, 1, 3, 5.0, 200.0, 0.0, 0.05, 0.1);

    @Comment("P90 settings")
    private GunSettings p90 = new GunSettings(50, "p90_mag", 0.06, 3.0, 1, 3, 5.0, 200.0, 0.0, 0.065, 0.1);

    @Comment("AWP settings")
    private GunSettings awp = new GunSettings(10, "awp_mag", 1.0, 5.0, 3, 10, 30.0, 600.0, 0.0, 0.25, 0.1);

    @Comment("Barrett settings")
    private GunSettings barrett = new GunSettings(10, "barrett_mag", 1.0, 5.0, 3, 10, 15.0, 1850.0, 0.0, 0.2, 0.1);

    public Map<String, GunSettings> guns() {
        Map<String, GunSettings> guns = new LinkedHashMap<>();
        guns.put("akm", akm);
        guns.put("mk18", mk18);
        guns.put("m249", m249);
        guns.put("taurus", taurus);
        guns.put("m3", m3);
        guns.put("mp5", mp5);
        guns.put("p90", p90);
        guns.put("awp", awp);
        guns.put("barrett", barrett);
        return guns;
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static class GunSettings extends OkaeriConfig {

        private int magSize;
        private String magName;
        private double fireCoolDown;
        private double reloadTime;
        private int slownessLevel;
        private int slownessLevelAim;
        private double hitDamage;
        private double range;
        private double recoil;
        private double fireSwingIntensity;
        private double fireSwingDuration;
        private String particle = "minecraft:basic_crit_particle";

        public GunSettings() {
        }

        private GunSettings(int magSize, String magName, double fireCoolDown, double reloadTime,
                            int slownessLevel, int slownessLevelAim, double hitDamage, double range,
                            double recoil, double fireSwingIntensity, double fireSwingDuration) {
            this.magSize = magSize;
            this.magName = magName;
            this.fireCoolDown = fireCoolDown;
            this.reloadTime = reloadTime;
            this.slownessLevel = slownessLevel;
            this.slownessLevelAim = slownessLevelAim;
            this.hitDamage = hitDamage;
            this.range = range;
            this.recoil = recoil;
            this.fireSwingIntensity = fireSwingIntensity;
            this.fireSwingDuration = fireSwingDuration;
        }
    }
}
