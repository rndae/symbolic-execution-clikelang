
class Node {
    String key;
    Node left, right;
    Node parent;

    public Node(String item)
    {
        key = item;
        left = right = null;
    }

    public Node(String item, Node parent)
    {
        key = item;
        left = right = null;
        this.parent = parent;
    }
}
