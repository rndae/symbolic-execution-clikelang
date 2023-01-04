import java.util.LinkedList;

class BinaryTree {

    Node root;
    Node current;

    BinaryTree(String key) {
        root = new Node(key);
        current = root;
        current.parent = null;
    }

    public LinkedList<Node> getLeaves(){
        LinkedList<Node> result = new LinkedList<>();
        playAutumnLeaves(root, result);
        return result;
    }

    public void playAutumnLeaves(Node node, LinkedList<Node> result) {
        if (node != null) {
            if (node.left == null && node.right == null){
                result.add(node);
            } else {
                playAutumnLeaves(node.left, result);
                playAutumnLeaves(node.right, result);
            }
        }
    }

    public LinkedList<Node>  findTheRootOfAllEvil(Node node) {
        LinkedList<Node> result = new LinkedList<>();
        findRoot(node, result);
        return result;
    }

    public void findRoot(Node from, LinkedList<Node> result) {
        if (from != null) {
            result.add(from);
            findRoot(from.parent, result);
        }
    }

}