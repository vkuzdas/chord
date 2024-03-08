import chord.ChordNode;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class BigTest {
    static int FIX_FINGER_TIMEOUT;
    private final ArrayList<ChordNode> toShutdown = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        FIX_FINGER_TIMEOUT = 4*ChordNode.m * ChordNode.STABILIZATION_INTERVAL;
    }

    @BeforeEach
    void init(TestInfo testInfo) {
        System.out.println(System.lineSeparator() + System.lineSeparator()
                + "============== " + testInfo.getTestMethod().map(Method::getName).orElse(null)
                + "() =============" + System.lineSeparator());
    }

    @AfterEach
    void tearDown() {
        toShutdown.forEach(ChordNode::awaitStopServer);
    }

    private void registerForShutdown(ChordNode ... nodes) {
        Collections.addAll(toShutdown, nodes);
    }

    @Test
    void testBigLoad() throws IOException, InterruptedException {
        ChordNode.STABILIZATION_INTERVAL = 500;
        ChordNode.m = 10;

        ChordNode bootstrap = new ChordNode("localhost", 9000);
        bootstrap.createRing();

        ArrayList<ChordNode> nodes = new ArrayList<>();
        for (int i = 9001; i < 9010 ; i++) {
            ChordNode n = new ChordNode("localhost", i);
            nodes.add(n);
            n.join(bootstrap);
        }

        bootstrap.blockUntilShutdown();


//        ArrayList<String> keys = new ArrayList<>();
//        Random rand = new Random();
//        for (int i = 0; i < 100; i++) {
//            int randomNumber = rand.nextInt(101) + 9000; // [9000, 9100)
//            nodes.get(randomNumber).put("key" + i, "value" + i);
//        }


    }

}
