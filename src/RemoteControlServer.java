import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author 14mRh4X0r
 */
public class RemoteControlServer {
    public static final Logger log = RemoteControl.log;
    public static ArrayList<Client> clients = new ArrayList<Client>();
    public static boolean loop = true;
    protected static ServerSocket ss;

    public static void serverLoop() {
        try {
            ss = new ServerSocket(RemoteControl.props.getInt("port", 11946));
        } catch (IOException e) {
            log.severe("RemoteControl could not bind to port");
        } try {
            while (loop) {
                Client c = new Client(ss.accept());
                clients.add(c);
                c.setName(c.getName().replace("Thread", "Client"));
                c.start();
            }
        } catch (IOException e) {
            log.info("RemoteControl could not set up connection to client"
                    + " or was disabled");
        } catch (NullPointerException e) {
            //duhr, ss failed.
        }
    }

}
