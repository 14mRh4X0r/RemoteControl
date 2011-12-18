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
