import java.io.IOException;

public class AVLHash {
	int sizeofhash;
	AVLTree[] a;

	public AVLHash(int s) {
		if (s % 2 == 0)
			sizeofhash = s + 1;
		else
			sizeofhash = s;
		a = new AVLTree[sizeofhash + 1];
	}

	// Method to insert key element pairs in the buckets
	public void insert(int i, int j) throws IOException {
		int bucket = 1;
		bucket = Math.abs(i) % sizeofhash;
		if (bucket == 0) {
			// System.out.println(sizeofhash);
			bucket = sizeofhash;
		}
		if (a[bucket] == null) {
			// System.out.println("If the tree is Null");
			a[bucket] = new AVLTree();
			a[bucket].insert(i, j);
		} else {
			// System.out.println("Tree is not null");
			a[bucket].insert(i, j);
		}
	}

	// Method to search a key in the buckets
	public void search(int i) {
		int bucket = i % sizeofhash;
		if (bucket == 0) {
			bucket = sizeofhash;
		}
		if (a[bucket] == null) {
			// System.out.println("Tree is Null. Hence the Item is not found");
		} else {
			// System.out.println("Tree is not null");
			a[bucket].search(i);
		}
	}
}
