import chord.ChordNode;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void testBig_put_get() throws IOException, InterruptedException {
        ChordNode.STABILIZATION_INTERVAL = 500;
        ChordNode.m = 50; // size of id has significant impact on speed of RPC calls

        ChordNode bootstrap = new ChordNode("localhost", 9100);
        bootstrap.createRing();

        ArrayList<ChordNode> nodes = new ArrayList<>();
        nodes.add(bootstrap);
        // start nodes
        for (int i = 9101; i < 9110 ; i++) { // depends on your machine how many nodes it can run
            ChordNode n = new ChordNode("localhost", i);
            nodes.add(n);
            n.join(bootstrap);
        }

        ArrayList<String> inserted = new ArrayList<>();
        Random rand = new Random();
        int node;
        // put to random node
        for (int i = 0; i < 500; i++) {
            node = rand.nextInt(nodes.size());
            nodes.get(node).put("key"+i, "value"+i);
            inserted.add("value"+i);
        }

        // two nodes leave
        node = rand.nextInt(nodes.size());
        nodes.get(node).leave();
        nodes.remove(node);

        node = rand.nextInt(nodes.size());
        nodes.get(node).leave();
        nodes.remove(node);

        Thread.sleep(5000);
        System.out.println("Chord stabilized after 5s");


        ArrayList<String> fetched = new ArrayList<>();
        // get
        for (int i = 0; i < 500; i++) {
            node = rand.nextInt(nodes.size());
            String v = nodes.get(node).get("key"+i);
            fetched.add(v);
        }

        assertEquals(inserted, fetched);
    }

    @Test
    void testBig_put_delete() throws IOException, InterruptedException {
        ChordNode.STABILIZATION_INTERVAL = 500;
        ChordNode.m = 50; // size of id has significant impact on speed of RPC calls

        ChordNode bootstrap = new ChordNode("localhost", 9100);
        bootstrap.createRing();

        ArrayList<ChordNode> nodes = new ArrayList<>();
        nodes.add(bootstrap);
        // start nodes
        for (int i = 9101; i < 9110 ; i++) { // depends on your machine how many nodes it can run
            ChordNode n = new ChordNode("localhost", i);
            nodes.add(n);
            n.join(bootstrap);
        }

        ArrayList<String> inserted = new ArrayList<>();
        Random rand = new Random();
        int node;
        // put to random node
        for (int i = 0; i < 500; i++) {
            node = rand.nextInt(nodes.size());
            nodes.get(node).put("key"+i, "value"+i);
            inserted.add("value"+i);
        }

        // two nodes leave
        node = rand.nextInt(nodes.size());
        nodes.get(node).leave();
        nodes.remove(node);

        node = rand.nextInt(nodes.size());
        nodes.get(node).leave();
        nodes.remove(node);

        Thread.sleep(5000);
        System.out.println("Chord stabilized after 5s");


        ArrayList<String> deleted = new ArrayList<>();
        // delete half of the keys
        for (int i = 0; i < 500; i++) {
            node = rand.nextInt(nodes.size());
            nodes.get(node).delete("key"+i);
            deleted.add("key"+i);
        }

        for(String key : deleted) {
            node = rand.nextInt(nodes.size());
            String v = nodes.get(node).get(key);
            assertNull(v);
        }

    }

}
