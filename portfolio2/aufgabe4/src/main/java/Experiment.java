import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;


public class Experiment {
    private byte[] payload = new byte[0];

    private int recursions;
    private List<Long> sendTimes;
    private List<Long> receiveTimes;

    private ZContext context;
    private Socket sender;
    private Socket receiver;

    public Experiment(int recursions) {
        this.recursions = recursions;
        this.sendTimes = new ArrayList<>(recursions);
        this.receiveTimes = new ArrayList<>(recursions);
        
        // Handle ZeroMQ context and sockets
        context = new ZContext();
        receiver = context.createSocket(SocketType.PULL);
        receiver.connect("tcp://reader:4242");
        sender = context.createSocket(SocketType.PUSH);
        sender.connect("tcp://reader:4200");
    }
    
    /**
     * Runs the experiment with the recursion count set in the constructor
     * @return Minimum latency value
     */
    public long runExperiment() throws Exception {
        sendTimes.clear();
        receiveTimes.clear();
        measure(recursions);
        List<Long> latencies = calculateLatencies();
        return Collections.min(latencies);
    }

    public void finish() {
        sender.close();
        receiver.close();
        context.close();
    }

    private void measure(int recursions) throws InterruptedException {
        long sendTime;
        long receiveTime;

        for (int i = 0; i < recursions; i++) {
            sendTime = System.nanoTime();
            sender.send(payload);
            receiver.recv();
            receiveTime = System.nanoTime();

            sendTimes.add(sendTime);
            receiveTimes.add(receiveTime);
        }
    }

    private List<Long> calculateLatencies() throws Exception {
        if (sendTimes.size() != receiveTimes.size()) {
            throw new Exception("sendTimes and receiveTimes are not of equal size");
        }

        List<Long> results = new ArrayList<>(sendTimes.size());

        for (int i = 0; i < sendTimes.size(); i++) {
            long latency = (receiveTimes.get(i) - sendTimes.get(i))/2;
            results.add(latency);
        }

        return results;
    }
}
