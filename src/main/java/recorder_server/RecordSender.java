package recorder_server;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import proto.RecordOuterClass;
import proto.RecorderGrpc;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordSender {
    private static final Logger logger = Logger.getLogger(RecordSender.class.getName());

    private final RecorderGrpc.RecorderBlockingStub blockingStub;

    /** Construct client for accessing RouteGuide server using the existing channel. */
    public RecordSender(Channel channel) {
        blockingStub = RecorderGrpc.newBlockingStub(channel);
    }

    public void sendRecord(String record) {
        RecordOuterClass.Record request = RecordOuterClass.Record.newBuilder().setRecord(record).build();

        RecordOuterClass.Record response;
        response = blockingStub.receiveRecord(request);
        logger.info("response: " + response.getRecord() + " from server");
    }

    private void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    private void warning(String msg, Object... params) {
        logger.log(Level.WARNING, msg, params);
    }


    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8980").usePlaintext().build();
        RecordSender client = new RecordSender(channel);
        client.sendRecord("something to send");
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
}
