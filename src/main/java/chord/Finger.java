package chord;

public class Finger {
    public final int start;
    public final int end;
    public NodeReference node; // successor(F[i].start)

    public Finger(int start, int end, NodeReference node) {
        this.start = start;
        this.end = end;
        this.node = node;
    }

    public void setNode(NodeReference node) {
        this.node = node;
    }
}
