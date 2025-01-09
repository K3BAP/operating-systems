import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class Reader {
    private static byte[] payload = new byte[0];

    public static void main(String[] args) {
        try(ZContext context = new ZContext()) {
            Socket receiver = context.createSocket(SocketType.PULL);
            receiver.bind("tcp://*:4200");

            Socket sender = context.createSocket(SocketType.PUSH);
            sender.bind("tcp://*:4242");

            System.out.println("Listening now");
            while (true) {
                receiver.recv();
                sender.send(payload);
            }
        }
        catch (Exception e) {
            System.out.println("Reader is shutting down");
        }
    }
}
