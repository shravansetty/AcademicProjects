import java.util.TreeMap;

public class RedBlackHash {
	int sizeofhash;
	TreeMap[] a;

	public RedBlackHash(int s) {
		// TODO Auto-generated constructor stub
		if (s % 2 == 0)
			sizeofhash = s + 1;
		else
			sizeofhash = s;
		// System.out.println(sizeofhash);
		a = new TreeMap[sizeofhash + 1];
		// System.out.println(a.length);
		// System.out.println(a[0]);
	}
	// Method to insert key element pairs in the buckets
	public void insert(int i, int j) {
		// TODO Auto-generated method stub
		int bucket = 1;
		bucket = Math.abs(i) % sizeofhash;
		if (bucket == 0) {
			// System.out.println(sizeofhash);
			bucket = sizeofhash;
		}
		// System.out.println(bucket);
		if (a[bucket] == null) {
			// System.out.println("TreeMap is Null");
			a[bucket] = new TreeMap<Integer, Integer>();
			a[bucket].put(i, j);
		} else {
			// System.out.println("TreeMap is not null");
			a[bucket].put(i, j);
		}
	}
	//Method to search a key in the buckets
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
			a[bucket].containsKey(i);
		}
	}
}