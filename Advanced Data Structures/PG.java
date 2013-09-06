import java.util.ArrayList;

//Permutation Generator Class
public class PG {
	int size;
	// Array
	ArrayList<Integer> A = new ArrayList<Integer>();
	// Random Generated Array
	ArrayList<Integer> RGA = new ArrayList<Integer>();

	public PG(int n) {
		size = n;
		for (int i = 1; i <= n; i++) {
			A.add(i);
		}
		for (int i = 1; i < n; i++) {
			int r = 0 + (int) (Math.random() * ((A.size() - 1 - 0) + 1));
			RGA.add(A.get(r));
			// System.out.println(" " + RGA.get(i - 1));
			A.remove(r);
		}
		RGA.add(A.get(0));
		A.remove(0);
		// Code commented below is to print the randomly generated array
		// for (int i = 1; i <= n; i++) {
		// System.out.print(" " + RGA.get(i - 1));
		// }
	}

}
