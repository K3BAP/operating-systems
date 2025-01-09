import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class Reader extends Thread {
    private ZContext context;
    private byte[] payload = new byte[0];

    public Reader(ZContext context) {
        this.context = context;
    }

    @Override
    public void run() {
            Socket receiver = context.createSocket(SocketType.PULL);
            receiver.connect("inproc://message");

            Socket sender = context.createSocket(SocketType.PUSH);
            sender.connect("inproc://response");

            while (!isInterrupted()) {
                receiver.recv();
                sender.send(payload);
            }
    }
}
