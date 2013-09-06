import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;

public class AVLTree {
	Node root;

	// Default Constructor
	public AVLTree() {
		// Default Constructor

	}

	// Method to Insert a Node into the AVL Tree
	public void insert(int key, int element1) {
		// Creating a Stack to store the path of insert
		Stack<Node> S = new Stack<Node>();
		// if the tree is empty, create a root node and assign key value and
		// element.
		if (root == null) {
			root = new Node(key, element1);
		} else {
			// if the tree is not empty. Create a Node
			Node a = root;
			Node nn = new Node(key, element1);
			// Inserting a node in Iterative way
			// Also Push the Values to stack to know the path
			while (true) {
				S.push(a);
				// New node value < current root value
				if (nn.value < a.value) {
					// if the left of the parent is null. Insert the node and
					// set balance factor to 0
					if (a.left == null) {
						a.left = nn;
						nn.balfac = 0;
						break;
					}
					// if not null,repeat the process with current root.left
					else
						a = a.left;
					// New node value > current root value
				} else if (nn.value > a.value) {
					// if the left of the parent is null. Insert the node set
					// balance factor to 0
					if (a.right == null) {
						a.right = nn;
						nn.balfac = 0;
						break;
					} // if not null,repeat the process with current root.right
					else
						a = a.right;
				}
				// if the value being inserted already exists in the tree: Exit
				else {
					System.out
							.println("No Duplicates are allowed in this application");
					System.exit(1);
				}
			}
			// Code to find the occurrence of imbalance
			Node temp1 = nn;
			/*
			 * Printing Stack Values System.out.println("Stack Values"); while
			 * (!S.empty()) { System.out.println(S.pop().value); }
			 */
			while (!S.empty()) {
				Node temp = S.pop();
				if (temp.left == temp1) {
					temp.balfac = temp.balfac + 1;
					if (temp.balfac == 0)
						break;
					else if (temp.balfac == 2 || temp.balfac == -2) {
						if (!S.empty()) {
							find_fix(temp, nn, S.pop());
						} else
							find_fix(temp, nn, null);
						break;
					}
					temp1 = temp;
				} else if (temp.right == temp1) {
					temp.balfac = temp.balfac - 1;
					if (temp.balfac == 0)
						break;
					else if (temp.balfac == 2 || temp.balfac == -2) {
						if (!S.empty()) {
							find_fix(temp, nn, S.pop());
						} else
							find_fix(temp, nn, null);
						break;
					}
					temp1 = temp;
				}
			}
		}
	}

	// Method to find A,B,C Nodes when the imbalance is found
	public void find_fix(Node temp, Node nn, Node Parent) {
		// System.out.println("Inside find & fix");
		Node A, B, C;
		A = temp;
		if (nn.value < A.value) {
			B = A.left;
			if (nn.value < B.value)
				C = B.left;
			else
				C = B.right;
		} else {
			B = A.right;
			if (nn.value < B.value)
				C = B.left;
			else
				C = B.right;
		}
		rotate_main(A, B, C, Parent);

	}

	// Method to determine the type of imbalance and call the appropriate
	// rotations
	public void rotate_main(Node A, Node B, Node C, Node Parent) {
		// System.out.print("Rotate Imbalance Type: ");
		if (A.left == B && B.left == C) {
			// System.out.println(" LL");
			LL(A, B, C, Parent);
		} else if (A.right == B && B.right == C) {
			// System.out.println(" RR");
			RR(A, B, C, Parent);
		} else if (A.right == B && B.left == C) {
			// System.out.println(" RL");
			RL(A, B, C, Parent);
		} else if (A.left == B && B.right == C) {
			// System.out.println(" LR");
			LR(A, B, C, Parent);
		}
	}

	// LL Rotate Method
	public void LL(Node A, Node B, Node C, Node Parent) {
		// System.out.println("LL Actual");
		if (Parent == null)
			root = B;
		else {
			if (Parent.left == A)
				Parent.left = B;
			else
				Parent.right = B;
		}
		A.left = B.right;
		B.right = A;
		// System.out.println(B.balfac);
		B.balfac = 0;
		A.balfac = 0;
	}

	// RR Rotate Method
	public void RR(Node A, Node B, Node C, Node Parent) {
		// System.out.println("RR Actual");
		if (Parent == null)
			root = B;
		else {
			if (Parent.left == A)
				Parent.left = B;
			else
				Parent.right = B;
		}
		A.right = B.left;
		B.left = A;
		// System.out.println("---" + B.balfac);
		B.balfac = 0;
		A.balfac = 0;
	}

	// LR Rotate Method
	public void LR(Node A, Node B, Node C, Node Parent) {
		int cas = 0;
		// System.out.println("LR Actual");
		if (A.balfac == 2 && B.balfac == -1 && C.balfac == 1) {
			// System.out.println(A.balfac + "==" + B.balfac + "==" + C.balfac);
			cas = 1;
		} else if (A.balfac == 2 && B.balfac == -1 && C.balfac == 1) {
			// System.out.println(A.balfac + "==" + B.balfac + "==" + C.balfac);
			cas = 2;
		}
		RR(B, C, null, A);
		LL(A, C, B, Parent);
		switch (cas) {
		case 1: {
			C.balfac = 0;
			B.balfac = 0;
			A.balfac = -1;
			break;
		}
		case 2: {
			B.balfac = 1;
			A.balfac = 0;
			C.balfac = 0;
			break;
		}
		default:
		}

	}

	// RL Rotate Method
	public void RL(Node A, Node B, Node C, Node Parent) {
		int cas = 0;
		// System.out.println("RL Actual");
		if (A.balfac == -2 && B.balfac == 1 && C.balfac == -1) {
			// System.out.println(A.balfac + "==" + B.balfac + "==" + C.balfac);
			cas = 1;
		} else if (A.balfac == -2 && B.balfac == 1 && C.balfac == -1) {
			// System.out.println(A.balfac + "==" + B.balfac + "==" + C.balfac);
			cas = 2;
		}
		LL(B, C, null, A);
		RR(A, C, B, Parent);
		switch (cas) {
		case 1: {
			C.balfac = 0;
			B.balfac = 0;
			A.balfac = 1;
			break;
		}
		case 2: {
			B.balfac = -1;
			A.balfac = 0;
			C.balfac = 0;
			break;
		}
		default:
		}
	}

	// Methods to print the In-Order walk
	public void inorder_walk(Node root) {
		if (root != null) {
			inorder_walk(root.left);
			// System.out.print(root.value + "  ");
			// System.out.println(root.balfac);
			inorder_walk(root.right);
		}
	}

	public void inorder_walk(Node root, BufferedWriter bw) {
		if (root != null) {
			inorder_walk(root.left, bw);
			// System.out.println(root.value + "  ");
			try {
				// bw.write(Integer.toString(root.value));
				bw.write(Integer.toString(root.element));
				bw.write(" ");
			} catch (IOException e) {
				e.printStackTrace();
			}
			// System.out.println(root.balfac);
			inorder_walk(root.right, bw);
		}
	}

	// Methods to print the Post-Order walk
	public void postorder_walk(Node root) {
		if (root != null) {
			postorder_walk(root.left);
			postorder_walk(root.right);
			// System.out.print(root.value + "  ");
			// System.out.println(root.balfac);
		}
	}

	public void postorder_walk(Node root, BufferedWriter bw) {
		if (root != null) {
			postorder_walk(root.left, bw);
			postorder_walk(root.right, bw);
			// System.out.println(root.value + "  ");
			// System.out.println(root.balfac);
			try {
				bw.write(Integer.toString(root.element));
				bw.write(" ");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	// Method to search a key in the AVL Tree
	public void search(int i) {
		search(i, root);
	}

	public void search(int key, Node a) {
		if (a != null) {
			if (a.value == key) {
				// System.out.println("Key Found");
			} else if (key < a.value) {
				a = a.left;
				search(key, a);
			} else {
				a = a.right;
				search(key, a);
			}
		}
	}
}