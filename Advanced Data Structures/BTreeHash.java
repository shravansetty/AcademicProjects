import java.io.IOException;

public class BTreeHash {

	int sizeofhash;
	BTree[] a;

	public BTreeHash(int s) {
		// TODO Auto-generated constructor stub
		if (s % 2 == 0)
			sizeofhash = s + 1;
		else
			sizeofhash = s;
		// System.out.println(sizeofhash);
		a = new BTree[sizeofhash + 1];
		// System.out.println(a.length);
		// System.out.println(a[0]);
	}

	// Method to insert the key element pairs into appropriate buckets
	public void insert(int i, int j) throws IOException {
		// TODO Auto-generated method stub
		int bucket = 1;
		bucket = Math.abs(i) % sizeofhash;
		if (bucket == 0) {
			// System.out.println(sizeofhash);
			bucket = sizeofhash;
		}
		// System.out.println(bucket);
		if (a[bucket] == null) {
			// System.out.println("Tree is Null");
			a[bucket] = new BTree();
			a[bucket].insert(i, j);
		} else {
			// System.out.println("Tree is not null");
			a[bucket].insert(i, j);
		}
	}

	// Method to search a key in appropriate bucket.
	public void search(int i) {
		// TODO Auto-generated method stub
		int bucket = i % sizeofhash;
		if (bucket == 0) {
			// System.out.println(sizeofhash);
			bucket = sizeofhash;
		}
		// System.out.println(bucket);
		if (a[bucket] == null) {
			// System.out.println("Tree is Null. Hence the Item is not found");
		} else {
			// System.out.println("Tree is not null");
			a[bucket].Search(a[bucket].root, i);
		}
	}

}
