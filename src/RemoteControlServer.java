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
    static boolean loop = true;
    static ServerSocket ss;

    public static void serverLoop() {
        try {
            ss = new ServerSocket(RemoteControl.props.getInt("port", 11946));
        } catch (IOException e) {
            log.severe("RemoteControl could not bind to port");
        } try {
            while (loop) {
                Client c = RemoteControl.isParanoid()
                        ? new SecureClient(ss.accept())
                        : new NormalClient(ss.accept());
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
