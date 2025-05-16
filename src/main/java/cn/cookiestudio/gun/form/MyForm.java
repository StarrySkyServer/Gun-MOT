package cn.cookiestudio.gun.form;


import cn.cookiestudio.gun.GunPlugin;
import cn.cookiestudio.gun.form.easy_form.Custom;
import cn.cookiestudio.gun.form.easy_form.Simple;
import cn.cookiestudio.gun.guns.GunData;
import cn.cookiestudio.gun.guns.ItemGunBase;
import cn.cookiestudio.gun.playersetting.PlayerSettingMap;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;

import java.util.ArrayList;
import java.util.List;

public class MyForm {
    public static void modifyGunData(Player player) {
        Simple simple = new Simple("选择你需要修改参数的枪械:", "", true);
        GunPlugin.getInstance().getGunDataMap().values().forEach(gunData -> {
            simple.add(gunData.getGunName(), "textures/items/book_writable", () -> {
                String gunName = simple.getClickedText();
                Class<? extends ItemGunBase> gunClass = GunPlugin.getInstance().getStringClassMap().get(gunName);
                GunData gunData1 = GunPlugin.getInstance().getGunDataMap().get(gunClass);
                Custom custom = new Custom(gunData1.getGunName());
                custom.add("弹夹容量", new ElementInput("弹夹容量", "magSize", String.valueOf(gunData.getMagSize())));
                custom.add("开火冷却", new ElementInput("开火冷却", "fireCoolDown", String.valueOf(gunData.getFireCoolDown())));
                custom.add("换弹时间", new ElementInput("换弹时间", "reloadTime", String.valueOf(gunData.getReloadTime())));
                custom.add("站立时缓慢等级", new ElementInput("站立时缓慢等级", "slownessLevel", String.valueOf(gunData.getSlownessLevel())));
                custom.add("潜行时缓慢等级", new ElementInput("潜行时缓慢等级", "slownessLevelAim", String.valueOf(gunData.getSlownessLevelAim())));
                custom.add("开火视角摇晃程度", new ElementInput("开火视角摇晃程度", "fireSwingIntensity", String.valueOf(gunData.getFireSwingIntensity())));
                custom.add("伤害", new ElementInput("伤害", "hitDamage", String.valueOf(gunData.getHitDamage())));
                custom.add("范围", new ElementInput("范围", "range", String.valueOf(gunData.getRange())));
                custom.add("弹道粒子效果", new ElementInput("弹道粒子效果", "particle", gunData.getParticle()));
                custom.add("弹夹名称", new ElementInput("弹夹名称", "magName", gunData.getMagName()));
                custom.add("后坐力", new ElementInput("后坐力", "recoil", String.valueOf(gunData.getRecoil())));
                custom.add("开火视角摇晃时间", new ElementInput("开火视角摇晃时间", "fireSwingDuration", String.valueOf(gunData.getFireSwingDuration())));
                custom.setSubmit(() -> {
                    gunData1.setMagSize(Integer.parseInt(custom.getInputRes("弹夹容量")));
                    gunData1.setFireCoolDown(Double.parseDouble(custom.getInputRes("开火冷却")));
                    gunData1.setReloadTime(Double.parseDouble(custom.getInputRes("换弹时间")));
                    gunData1.setSlownessLevel(Integer.parseInt(custom.getInputRes("站立时缓慢等级")));
                    gunData1.setSlownessLevelAim(Integer.parseInt(custom.getInputRes("潜行时缓慢等级")));
                    gunData1.setFireSwingIntensity(Double.parseDouble(custom.getInputRes("开火视角摇晃程度")));
                    gunData1.setHitDamage(Double.parseDouble(custom.getInputRes("伤害")));
                    gunData1.setRange(Double.parseDouble(custom.getInputRes("范围")));
                    gunData1.setParticle(custom.getInputRes("弹道粒子效果"));
                    gunData1.setMagName(custom.getInputRes("弹夹名称"));
                    gunData1.setRecoil(Double.parseDouble(custom.getInputRes("后坐力")));
                    gunData1.setFireSwingDuration(Double.parseDouble(custom.getInputRes("开火视角摇晃时间")));
                    GunPlugin.getInstance().saveGunData(gunData);
                    player.sendMessage("§a成功!");
                });
                custom.show(player);
            });
        });
        simple.show(player);
    }

    public static void modifyGunSetting(Player player) {
        Custom custom = new Custom("设置");
        PlayerSettingMap settings = GunPlugin.getInstance().getPlayerSettingPool().getPlayerSetting(player.getName());
        List<String> list = new ArrayList<>();
        list.add(PlayerSettingMap.FireMode.AUTO.name());
        list.add(PlayerSettingMap.FireMode.MANUAL.name());
        custom.add("开火模式", new ElementDropdown("开火模式:", list, settings.getFireMode().ordinal()));
        custom.add("打开弹道粒子", new ElementToggle("打开弹道粒子:", settings.isOpenTrajectoryParticle()));
        custom.add("打开开火烟雾", new ElementToggle("打开开火烟雾:", settings.isOpenMuzzleParticle()));
        custom.setSubmit(() -> {
            FormResponseCustom response = custom.getForm().getResponse();
            if (response.getDropdownResponse(0).getElementContent().equals(PlayerSettingMap.FireMode.AUTO.name())) {
                settings.setFireMode(PlayerSettingMap.FireMode.AUTO);
            } else {
                settings.setFireMode(PlayerSettingMap.FireMode.MANUAL);
            }
            settings.setOpenTrajectoryParticle(response.getToggleResponse(1));
            settings.setOpenMuzzleParticle(response.getToggleResponse(2));
            GunPlugin.getInstance().getPlayerSettingPool().write(player.getName(), settings);
            player.sendMessage("§a成功!");
        });
        custom.show(player);
    }
}
