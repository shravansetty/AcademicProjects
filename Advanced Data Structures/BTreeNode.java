import java.util.ArrayList;

public class BTreeNode {
	static int btree_order;
	BTreeNode[] pointers;
	ArrayList<KEP> pairs;
	boolean leaf;
	int n;
// Constructor that takes input as keyelement pair and order
	public BTreeNode(KEP k1, int order) {
		btree_order = order;
		initArrays();
		// System.out.println(pairs.length);
		// System.out.println(i);
		pairs.add(k1);
		// System.out.println("order");

	}
// Constructor that takes only order as input
	public BTreeNode(int order) {
		// Default Constructor
		btree_order = order;
		initArrays();
	}
// Method to create the arrays to store the pointers and KEP Objects
	public void initArrays() {
		pairs = new ArrayList<KEP>();
		pointers = new BTreeNode[btree_order + 1];
	}
}
