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

import java.util.logging.LogRecord;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 *
 * @author 14mRh4X0r
 */
class RemoteControlHandler extends Handler {

    public RemoteControlHandler() {
        setFormatter(Logger.getLogger("Minecraft").getHandlers()[0]
                .getFormatter());
    }

    public void publish(LogRecord logRecord) {
        for (Client client : RemoteControlServer.clients) {
            client.sendMessage(getFormatter().format(logRecord));
        }
    }

    public void flush() {
    }

    public void close() throws SecurityException {
    }

}
