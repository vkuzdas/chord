package chord;

import static chord.ChordNode.calculateSHA1;

public class NodeReference {
    public final String ip;
    public final int port;
    public int id;

    public NodeReference(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.id = calculateSHA1(this.toString());
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }

    @Override
    public boolean equals(Object obj) {
        NodeReference other = (NodeReference) obj;
        return this.port == other.port && this.ip.equals(other.ip);
    }
}
