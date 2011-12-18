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
    private static PluginListener pl = new PluginListener(){
        @Override
        public boolean onCommand(Player player, String[] split) {
            if (split[0].equalsIgnoreCase("/listclients")
                    && player.canUseCommand("/listclients")) {
                String clients = "";
                for (Client c: RemoteControlServer.clients) {
                    if (!clients.isEmpty()) clients += ", ";
                    clients += c.getName();
                }
                player.sendMessage(Colors.Gold + "RemoteControl clients: "
                        + clients);
                return true;
            } else if (split[0].equalsIgnoreCase("/killclient")
                    && player.canUseCommand("/killclient")) {
                for (Client c: RemoteControlServer.clients) {
                    if (c.getName().equalsIgnoreCase(split[1])
                            || c.getName()
                                .equalsIgnoreCase("Client-" + split[1])) {
                        c.parse('q', null);
                        player.sendMessage(Colors.Gold + "Killed " + split[1]);
                        return true;
                    }
                }
                player.sendMessage(Colors.Gold + "No such client: " + split[1]);
                return true;
            } else return false;
        }

        @Override
        public boolean onConsoleCommand(String[] split) {
            String cmd = "";
            for (String el: split) {
                if (!cmd.isEmpty()) cmd += " ";
                cmd += el;
            }
            String name = Thread.currentThread().getName();
            if (name.startsWith("Thread")) name = "CONSOLE";
            log.info("[" + name + "] issued command: " + cmd);
            if (split[0].equalsIgnoreCase("listclients")) {
                String clients = "";
                for (Client c: RemoteControlServer.clients) {
                    if (!clients.isEmpty()) clients += ", ";
                    clients += c.getName();
                }
                log.info("RemoteControl clients: " + clients);
                return true;
            } else if (split[0].equalsIgnoreCase("killclient")) {
                for (Client c: RemoteControlServer.clients) {
                    if (c.getName().equalsIgnoreCase(split[1]))
                        c.parse('q', null);
                }
                return true;
            } else if (split[0].equalsIgnoreCase("help")
                    || split[0].equalsIgnoreCase("?")
                    || split[0].equalsIgnoreCase("mod-help")) {
                log.info("RemoteControl plugin help:");
                log.info("listclients   Lists all RemoteControl clients");
                log.info("killclient    Kills a client");
            }
            return false;
        }
    };
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
