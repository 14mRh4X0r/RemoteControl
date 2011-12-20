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

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Logger;


/**
 *
 * @author 14mRh4X0r
 */
public class RemoteControl extends Plugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    public static final PropertiesFile props;
    private static final boolean paranoid;
    private static PluginRegisteredListener serverCommand;
    private static PluginRegisteredListener playerCommand;
    private static RemoteControl instance;
    public static final Handler handler = new RemoteControlHandler();
    private static PluginListener pl = new RemoteControlListener();
    private static Runnable serverLoop = new Runnable(){
        public void run() {
            RemoteControlServer.serverLoop();
        }
    };
    
    static {
        File dir = new File("plugins" + File.separator + "RemoteControl");

        if (!dir.exists()) {
            dir.mkdirs();
        }
        props = new PropertiesFile("plugins" + File.separator + "RemoteControl"
                + File.separator + "RemoteControl.properties");
        paranoid = props.getBoolean("paranoid", false);
    }

    public void enable() {
        log.addHandler(handler);
        instance = this;
        if (paranoid)
            KeyUtils.loadOrGenerateKeys();
        RemoteControlServer.loop = true;
        new Thread(serverLoop, "RCServer").start();
        etc.getInstance().addCommand("/listclients",
                "Lists all RemoteControl clients");
        etc.getInstance().addCommand("/killclient",
                "[Client] - Kicks a RemoteControl client");
        serverCommand = etc.getLoader().addListener(PluginLoader.Hook
                .SERVERCOMMAND, pl, this, PluginListener.Priority.CRITICAL);
        playerCommand = etc.getLoader().addListener(PluginLoader.Hook
                .COMMAND, pl, this, PluginListener.Priority.MEDIUM);
    }

    public void disable() {
        log.removeHandler(handler);
        RemoteControlServer.loop = false;
        for (Client c: RemoteControlServer.clients) {
            c.parse('q', null);
        }
        try {
            RemoteControlServer.ss.close();
        } catch (Exception e) {
            log.warning("D'aww, I could not close the listening socket.");
        }
        etc.getInstance().removeCommand("/listclients");
        etc.getInstance().removeCommand("/killclient");
        etc.getLoader().removeListener(serverCommand);
        etc.getLoader().removeListener(playerCommand);
    }

    public static RemoteControl getInstance() {
        return instance;
    }

    public static boolean isParanoid() {
        return paranoid;
    }

}
