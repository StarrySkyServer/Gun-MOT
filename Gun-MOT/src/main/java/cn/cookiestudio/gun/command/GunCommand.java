package cn.cookiestudio.gun.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParameter;

import static cn.cookiestudio.gun.form.MyForm.modifyGunData;
import static cn.cookiestudio.gun.form.MyForm.modifyGunSetting;

public class GunCommand extends Command {
    public GunCommand(String name) {
        super(name, "Gun Plugin Command");
        this.setPermission("gun.command");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newEnum("opt", new String[]{"data", "setting"})
        });
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof ConsoleCommandSender) {
            commandSender.sendMessage("此命令无法在控制台使用！");
            return true;
        }
        if (strings.length == 0) {
            return true;
        }
        Player player = (Player) commandSender;
        if (strings[0].equals("data")) {
            if (!commandSender.isOp()) {
                commandSender.sendMessage("你没有足够的权限使用此命令！");
                return true;
            }
            modifyGunData(player);
            return true;
        }
        if (strings[0].equals("setting")) {
            modifyGunSetting(player);
            return true;
        }
        return true;
    }
}
