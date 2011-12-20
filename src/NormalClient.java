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
public class NormalClient extends Client {
    private BufferedReader in;
    private PrintWriter out;

    public NormalClient(Socket sock) throws IOException {
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
