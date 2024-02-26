import chord.ChordNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChordNodeTest {

    @Test
    public void testNodeReference() throws Exception {
        assertEquals(ChordNode.m, 4); // Chord space assumption for the test

        ChordNode bootstrap = new ChordNode("localhost", 8980);
        bootstrap.start();

        bootstrap.put("icecream", "sweet");
        bootstrap.put("lollipop", "sour");

        assertEquals(bootstrap.getDataSize(), 2);

        ChordNode node2 = new ChordNode("localhost", 8981);
        node2.start();
        node2.join(bootstrap);


        assertEquals(bootstrap.getDataSize(), 1);
        assertEquals(node2.getDataSize(), 1);
    }

}
