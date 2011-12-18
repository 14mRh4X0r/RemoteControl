import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;

/**
 *
 * @author 14mRh4X0r
 */
public class Client extends Thread {
    protected Socket s;
    private BufferedReader in;
    private PrintWriter out;
    private boolean authenticated = false;
    private boolean run = true;

    public Client(Socket sock) throws IOException {
        s = sock;
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream());
        if (RemoteControl.props.getString("password", "").isEmpty())
            authenticated = true;
        setDaemon(true);
        RemoteControl.log.info(getName().replace("Thread", "Client") + " ["
                + s.getInetAddress() + ":" + s.getPort() + "] connected.");
    }

    @Override
    public void run() {
        while (run) {
            try {
                char mode = (char) in.read();
                if (mode == '\uffff') mode = 'q';
                parse(mode, in.readLine());
            } catch (IOException e) {
                RemoteControl.log.severe(e.getMessage());
                parse('q', null);
            }
        }
    }

    protected void parse(char mode, String in) {
        if (!authenticated && mode != 'a' && mode != 'q') return;
        switch (mode) {
            case 'a':
                if (in.isEmpty()) {
                    out.println(authenticated?'y':'n');
                    out.flush();
                }
                else if (!authenticated) authenticate(in);
                break;
            case 'i':
                if (!etc.getInstance().parseConsoleCommand(in,
                        etc.getMCServer()))
                    etc.getServer().useConsoleCommand(in);
                break;
            case 'q':
                run = false;
                RemoteControl.log.info(getName() + " [" + s.getInetAddress() + ":"
                        + s.getPort() + "] disconnected.");
                try {
                    s.close();
                } catch (Exception e) {}
                RemoteControlServer.clients.remove(this);
                break;
            case 'l':
                String outS = "";
                for (Player p: etc.getServer().getPlayerList()) {
                    if (!outS.isEmpty()) outS += ",";
                    outS += p.getName();
                }
                out.print('l');
                out.println(outS);
                out.flush();
                break;
        }
    }

    public void sendMessage(String formatMessage) {
        if (!authenticated) return;
        out.print('m');
        out.print(formatMessage);
        out.flush();
    }

    private void authenticate(String in) {
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String enc = new String(md5.digest(RemoteControl.props
                    .getString("password").getBytes()));
            if (in.equals(enc)) authenticated = true;
        } catch (Exception e) {}
    }
}
