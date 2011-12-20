/*
 * RemoteControl - A remote control plugin for CanaryMod
 * Copyright (C) 2011 Willem Mulder (14mRh4X0r)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * @author 14mRh4X0r
 */
class RemoteControlListener extends PluginListener {

    @Override
    public boolean onCommand(Player player, String[] split) {
        if (split[0].equalsIgnoreCase("/listclients") && player.canUseCommand("/listclients")) {
            String clients = "";
            for (Client c : RemoteControlServer.clients) {
                if (!clients.isEmpty()) {
                    clients += ", ";
                }
                clients += c.getName();
            }
            player.sendMessage(Colors.Gold + "RemoteControl clients: " + clients);
            return true;
        } else if (split[0].equalsIgnoreCase("/killclient") && player.canUseCommand("/killclient")) {
            for (Client c : RemoteControlServer.clients) {
                if (c.getName().equalsIgnoreCase(split[1]) || c.getName().equalsIgnoreCase("Client-" + split[1])) {
                    c.parse('q', null);
                    player.sendMessage(Colors.Gold + "Killed " + split[1]);
                    return true;
                }
            }
            player.sendMessage(Colors.Gold + "No such client: " + split[1]);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onConsoleCommand(String[] split) {
        String cmd = "";
        for (String el : split) {
            if (!cmd.isEmpty()) {
                cmd += " ";
            }
            cmd += el;
        }
        String name = Thread.currentThread().getName();
        if (name.startsWith("Thread")) {
            name = "CONSOLE";
        }
        RemoteControl.log.info("[" + name + "] issued command: " + cmd);
        if (split[0].equalsIgnoreCase("listclients")) {
            String clients = "";
            for (Client c : RemoteControlServer.clients) {
                if (!clients.isEmpty()) {
                    clients += ", ";
                }
                clients += c.getName();
            }
            RemoteControl.log.info("RemoteControl clients: " + clients);
            return true;
        } else if (split[0].equalsIgnoreCase("killclient")) {
            for (Client c : RemoteControlServer.clients) {
                if (c.getName().equalsIgnoreCase(split[1])) {
                    c.parse('q', null);
                }
            }
            return true;
        } else if (split[0].equalsIgnoreCase("help") || split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("mod-help")) {
            RemoteControl.log.info("RemoteControl plugin help:");
            RemoteControl.log.info("listclients   Lists all RemoteControl clients");
            RemoteControl.log.info("killclient    Kills a client");
        }
        return false;
    }
    
}
