import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class BTree {
	static int btree_order;
	static int node_count;
	static int level;
	BTreeNode root;

	// Constructor with no arguments. The order is hardcoded to 3 when default
	// constructor is called
	public BTree() {
		// Default Constructor
		btree_order = 3;
	}

	// Constructor which takes order as input
	public BTree(int order) {
		// Constructor which takes order as input
		if (order > 2) {
			btree_order = order;
		} else {
			System.out.println("Order of tree Should be greater than 2");
			System.exit(1);
		}
	}

	// Method to insert Key Value pair into
	public void insert(int key, int element) {
		Stack<BTreeNode> S = new Stack<BTreeNode>();
		if (root == null) {
			// If the Tree is empty, Create a root
			KEP k1 = new KEP(key, element);
			root = new BTreeNode(k1, btree_order);
			root.leaf = true;
			// node_count = node_count + 1;
		}
		// // Tree already has a root
		else {
			BTreeNode a = root;
			KEP k2 = new KEP(key, element);
			while (true) {
				// if (!a.equals(root)) {
				S.push(a);
				// }
				int i = get_index(a, k2);
				if (a.pairs.size() >= btree_order - 1) {
					// System.out.println("Node is Full");
					// if the Child node is not yet created, create node and add
					if (a.pointers[i] == null) {
						a.pairs.add(i, k2);
						// Check is Split is required
						if (a.pairs.size() > btree_order - 1) {
							if (!S.empty()) {
								S.pop();
								split(a, S);
							}
						}
						// a.pointers[i] = nn;
						break;
					}
					// if the child node is already created,just add the value
					else {
						a = a.pointers[i];
					}
					// if current node is NOT FULL. Add pair in correct position
				} else if (a.pairs.size() < btree_order - 1) {
					if (a.pointers[i] == null) {
						a.pairs.add(i, k2);
						break;
					} else {
						a = a.pointers[i];
					}

				}
			}
		}
	}

	// Method to Split the node
	private void split(BTreeNode a, Stack<BTreeNode> SS) {
		// System.out.println("Split Method");
		BTreeNode temp = a;
		BTreeNode nn = new BTreeNode(btree_order);
		nn.leaf = true;
		int split_index = (int) (btree_order - Math.ceil((btree_order) / 2) + 1);
		int root_index = (int) (btree_order - Math.ceil((btree_order) / 2));
		int j = 0;
		for (int i = split_index; i <= btree_order; i++) {
			nn.pairs.add(a.pairs.get(i - 1));
			nn.pointers[j] = a.pointers[i - 1];
			a.pointers[i - 1] = null;
			j = j + 1;
			nn.pointers[j] = a.pointers[i];
			a.pointers[i] = null;
			j = j + 1;
		}
		// if the new node created already has pointers, set the leaf field to
		// false.
		if (nn.pointers[0] != null) {
			nn.leaf = false;
		}
		a.pairs.subList(split_index - 1, btree_order).clear();
		// if the split node is root
		if (a.equals(root)) {
			root = new BTreeNode(btree_order);
			root.pairs.add(temp.pairs.get(root_index - 1));
			a.pairs.remove(root_index - 1);
			root.pointers[0] = a;
			root.pointers[1] = nn;
		}
		// if the split node is not root
		else {
			BTreeNode Parent = null;
			if (!SS.empty()) {
				Parent = SS.pop();
			}
			KEP key2add = a.pairs.get(root_index - 1);
			int index = get_index(Parent, key2add);
			Parent.pairs.add(index, key2add);
			// System.out.println(index);
			int idx = get_index(Parent, nn.pairs.get(0));
			for (int i = btree_order; i > idx; i--) {
				Parent.pointers[i] = Parent.pointers[i - 1];
			}
			Parent.pointers[idx] = nn;
			a.pairs.remove(root_index - 1);
			if (Parent.pairs.size() > btree_order - 1) {
				// System.out.println("  " + Parent.pairs.size());
				split(Parent, SS);
			}

		}
	}

	// Method to search the index of the nodes.
	private int get_index(BTreeNode x, KEP k) {
		// start binary search
		int middle = 0;
		int low = 0;
		int high = x.pairs.size() - 1;
		while (high >= low) {
			middle = (int) Math.ceil((low + high) / 2.0);
			// System.out.println(middle);
			if (k.key > x.pairs.get(middle).key) {
				low = middle + 1;
			} else {
				high = middle - 1;
			}
		}
		if (low == high) {
			if (k.key < x.pairs.get(0).key) {
				return 0;
			} else {
				return middle;
			}
		} else {
			return low;
		}
		// end binary search
	}

	// Method to search a key in the tree
	public void Search(BTreeNode p, int key) {
		int i = 1;
		while (i <= p.pairs.size() && key > p.pairs.get(i - 1).key) {
			i = i + 1;
		}
		// if the key is in the current node
		if (i <= p.pairs.size() && key == p.pairs.get(i - 1).key) {
			// System.out.println("Key Found");
		}
		// if the current node is a leaf
		else if (p.leaf) {
			// System.out.println("Key is not in the Tree");
		}
		// if the current node is not a leaf,drill down to the child
		else {
			if (p.pointers[i - 1] == null) {
				// System.out.println("Key is not in the Tree");
			} else
				Search(p.pointers[i - 1], key);
		}
	}

	// Method to print Sorted output
	public void walk_through(BTreeNode x, BufferedWriter bw) throws IOException {
		if (x.leaf)
			for (int i = 0; i < x.pairs.size(); i++) {
				if (x.pairs.get(i) != null) {
					bw.write(Integer.toString(x.pairs.get(i).element));
					bw.write(" ");
				}
			}
		else
			for (int i = 0; i < x.pointers.length; i++) {
				if (x.pointers[i] != null) {
					walk_through(x.pointers[i], bw);
					if (i < x.pairs.size()) {
						bw.write(Integer.toString(x.pairs.get(i).element));
						bw.write(" ");
					}
				}
			}
	}

	// Method to print the Level Out Values
	public void level_order(BTreeNode root, BufferedWriter bw)
			throws IOException {
		BTreeNode a = root;
		BTreeNode dummy = new BTreeNode(3);
		dummy.n = -1;
		ArrayList<BTreeNode> List = new ArrayList<BTreeNode>();
		List.add(a);
		List.add(dummy);
		while (!List.isEmpty()) {
			if (a.n != -1) {
				// if the node is not dummy node
				for (int i = 0; i < btree_order; i++) {
					if (i < a.pairs.size()) {
						bw.write(a.pairs.get(i).element + " ");
					}
					if (a.pointers[i] != null) {
						List.add(a.pointers[i]);
					}
				}
			}
			// if the node is dummy node
			else {
				if (List.size() > 1) {
					List.add(dummy);
					bw.newLine();
				}
			}
			// after the values at a particular level are read
			List.remove(0);
			if (!List.isEmpty()) {
				a = List.get(0);
			}
		}
	}
}