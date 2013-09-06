import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

// Author: Shravan Pentamsetty
// UF Id: 9862-5845
// ADS Project Fall 2012.
/* The purpose of this project is to compare the relative performance of 
 * AVL, red-black, and B-trees as well as their counterparts with a hash table front end.
 *  We shall focus only on the search and insert operations and require all keys to be distinct.
 *  */
// This File contains main method. Entry point when the program is executed
public class dictionary {
	// Creating variables required to read the file
	static ArrayList<Integer> KeyFile = new ArrayList<Integer>();
	static ArrayList<Integer> ElementFile = new ArrayList<Integer>();
	static File f1;
	static File f2;
	static File f3;
	static File f4;
	static File f5;
	static File f6;
	static BufferedWriter bw;

	// Read the Keys and Elements in a file and store the values in an Array
	// List
	private static void readfile(String filename) throws NumberFormatException,
			IOException {
		System.out.println("User Input Mode: File Name:: " + filename);
		File myFile = new File(filename);
		FileReader fileReader;
		try {
			fileReader = new FileReader(myFile);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = null;
			String delim = " ";
			int n = 0;
			int key = 0;
			int element = 0;
			// Getting the first line from the input file which is the
			// number of lines in the file
			if ((line = reader.readLine()) != null) {
				n = Integer.parseInt(line);
				// System.out.println(n);
			}
			// For loop to read the keys and elements from the file
			for (int i = 1; i <= n; i++) {
				if ((line = reader.readLine()) != null) {
					// System.out.println(line);
					String[] values = line.split(delim);
					// System.out.print(values[0] + "   ");
					// System.out.println(values[1]);
					key = Integer.parseInt(values[0]);
					element = Integer.parseInt(values[1]);
					// System.out.println(element);
					// Validating the i/p values so that they >= 0
					if (key <= 0 || element <= 0) {
						System.err
								.println("Error at Line "
										+ i
										+ " of the input file: Key and Element Values should be Greater than zero");
						System.exit(1);
					}
					// Add the keys and elements to the file
					KeyFile.add(key);
					ElementFile.add(element);
				} else
					System.err
							.println("Incorrect number of lines in the Input Text file "
									+ filename);
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		/*
		 * Code to read a file line by line while ((line = reader.readLine()) !=
		 * null) { System.out.println(line); }
		 */

	}

	// Create the necessary files when User Input mode is selected
	private static void createfiles() {
		f1 = new File("AVL_inorder.out");
		f2 = new File("AVL_postorder.out");
		f3 = new File("AVLHash_inorder.out");
		f4 = new File("BTree_sorted.out");
		f5 = new File("BTree_level.out");
		f6 = new File("BTreeHash_level.out");
		// Delete all the existing files
		f1.delete();
		f2.delete();
		f3.delete();
		f4.delete();
		f5.delete();
		f6.delete();
		// Creating New Files
		try {
			f1.createNewFile();
			f2.createNewFile();
			f3.createNewFile();
			f4.createNewFile();
			f5.createNewFile();
			f6.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Main Program
	public static void main(String[] args) throws NumberFormatException,
			IOException {
		// initialization of time variables
		long start = 0, stop = 0;
		long time = 0;
		long avl_time_i = 0, avlhash_time_i = 0;
		long redblack_time_i = 0, redblackhash_time_i = 0;
		long btree_time_i = 0, btreehash_time_i = 0;
		long avl_time_s = 0, avlhash_time_s = 0;
		long redblack_time_s = 0, redblackhash_time_s = 0;
		long btree_time_s = 0, btreehash_time_s = 0;
		/*
		 * ArrayList<Integer> a = new ArrayList<Integer>(5);
		 * a.ensureCapacity(5); a.add(3, 2); System.out.println(a.get(3));
		 */
		// Validate if the input is correct and branch accordingly
		if (args.length > 0 && args[0].equals("-r")) {
			System.out.println("Random Mode");
			System.out
					.println("****************************************************");
			// initializing arguments so that these values are taken if there is
			// number format exception
			int arg1 = 5;
			int arg2 = 3;
			try {
				arg1 = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.err.println("Number Format Exception: Argument 1");
				System.exit(1);
			}
			try {
				arg2 = Integer.parseInt(args[2]);
			} catch (Exception e) {
				System.err.println("Number Format Exception: Argument 2");
				System.exit(1);
			}
			// If the number entered is incorrect, it will take ten lakhs as the
			// default number.
			int numbers = 1000000;
			System.out.print("Enter the number of elements: ");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			try {
				numbers = Integer.parseInt(br.readLine());
				if (!(numbers > 0)) {
					System.err
							.println("Number should be greater than zero. Re-run the program");
					System.exit(1);
				}
			} catch (Exception ioe) {
				System.err.println("Number Format Exception");
				System.exit(1);
			}
			System.out
			.println("****************************************************");

			int s = arg1;
			int btree_order = arg2;
			PG PermGen = new PG(numbers);
			// for (int repeat = 1; repeat <= 10; repeat++) {
			AVLTree avlt = new AVLTree();
			AVLHash avlh = new AVLHash(s);
			TreeMap rb = new TreeMap();
			RedBlackHash rbh = new RedBlackHash(s);
			BTree bt = new BTree(btree_order);
			BTreeHash bth = new BTreeHash(s);

			// AVL Tree Insert
			start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				// System.out.println(PermGen.A.get(i));
				avlt.insert(PermGen.RGA.get(i), 2 * (PermGen.RGA.get(i)));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			avl_time_i = time + avl_time_i;

			// AVL Tree Search
			start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				// System.out.println(PermGen.A.get(i - 1));
				avlt.search(PermGen.RGA.get(i));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			avl_time_s = time + avl_time_s;

			// AVL Hash Insert
			start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				// System.out.println(PermGen.A.get(i-1));
				avlh.insert(PermGen.RGA.get(i), 2 * (PermGen.RGA.get(i)));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			avlhash_time_i = time + avlhash_time_i; //

			// AVL Hash Search // avlh.search(2); start =
			System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				// System.out.println(PermGen.A.get(i - 1));
				avlh.search(PermGen.RGA.get(i));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			avlhash_time_s = time + avlhash_time_s;

			// RedBlack Insert
			start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) { //
				// System.out.println(PermGen.A.get(i));
				rb.put(PermGen.RGA.get(i), 2 * (PermGen.RGA.get(i)));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			redblack_time_i = time + redblack_time_i;
			System.gc();

			// RedBlack Search
			start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				// System.out.println(PermGen.A.get(i - 1));
				rb.containsKey(PermGen.RGA.get(i));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			redblack_time_s = time + redblack_time_s; //

			// RedBlack Hash Insert
			// start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) { //
				// System.out.println(PermGen.A.get(i));
				rbh.insert(PermGen.RGA.get(i), 2 * (PermGen.RGA.get(i)));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			redblackhash_time_i = time + redblackhash_time_i; //

			// RedBlack Hash Search // rbh.search(2); start =
			System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				rbh.search(PermGen.RGA.get(i));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			redblackhash_time_s = time + redblackhash_time_s; //
			System.gc();

			// B-Tree Insert
			start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				// System.out.println(PermGen.A.get(i));
				bt.insert(PermGen.RGA.get(i), 2 * (PermGen.RGA.get(i)));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			btree_time_i = time + btree_time_i;

			// B-Tree Search
			start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				// System.out.println(PermGen.A.get(i - 1));
				bt.Search(bt.root, PermGen.RGA.get(i));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			btree_time_s = time + btree_time_s;

			// BTree Hash Insert
			start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				// System.out.println(PermGen.A.get(i));
				bth.insert(PermGen.RGA.get(i), 2 * (PermGen.RGA.get(i)));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			btreehash_time_i = time + btreehash_time_i;

			// BTree Hash Search
			start = System.currentTimeMillis();
			for (int i = 0; i < numbers; i++) {
				// System.out.println(PermGen.A.get(i - 1));
				bth.search(PermGen.RGA.get(i));
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			btreehash_time_s = time + btreehash_time_s;
			// Print the times taken for each operation
			System.out.println("Insertion into AVL Tree took " + avl_time_i
					+ " milliseconds");

			System.out.println("Search from AVL Tree took " + avl_time_s
					+ " milliseconds");
			System.out
					.println("****************************************************");

			System.out.println("Insertion into AVLHash took " + avlhash_time_i
					+ " milliseconds");
			System.out.println("Search from AVL hash took " + avlhash_time_s
					+ " milliseconds");
			System.out
					.println("****************************************************");
			System.out.println("Insertion into RedBlack tree took "
					+ redblack_time_i + " milliseconds");
			System.out.println("Search from RedBlack Tree took "
					+ redblack_time_s + " milliseconds");
			System.out
					.println("****************************************************");
			System.out.println("Insertion into RedBlack Hash took "
					+ redblackhash_time_i + " milliseconds");
			System.out.println("Search from RedBlack Hash took "
					+ redblackhash_time_s + " milliseconds");
			System.out
					.println("****************************************************");

			System.out.println("Insertion into BTree took " + btree_time_i
					+ " milliseconds");
			System.out.println("Search from B-Tree took " + btree_time_s
					+ " milliseconds");
			System.out
					.println("****************************************************");

			System.out.println("Insertion into BTreeHash took "
					+ btreehash_time_i + " milliseconds");
			System.out.println("Search from BTreeHash took " + btreehash_time_s
					+ " milliseconds");
		}

		// User Input Mode
		else if (args.length > 0 && args[0].equals("-u") && args[1] != null) {
			// read the input file
			readfile(args[1]);
			// Create files
			createfiles();
			AVLTree avlt = new AVLTree();
			AVLHash avlh = new AVLHash(3);
			BTree bt = new BTree(3);
			BTreeHash bth = new BTreeHash(3);

			// Insert into each of the trees
			for (int i = 0; i < KeyFile.size(); i++) {
				// AVL
				avlt.insert(KeyFile.get(i), ElementFile.get(i));
				// AVLH
				avlh.insert(KeyFile.get(i), ElementFile.get(i));
				// BTree
				bt.insert(KeyFile.get(i), ElementFile.get(i));
				// BTree Hash
				bth.insert(KeyFile.get(i), ElementFile.get(i));
			}

			// inorder and postorder walks of AVL Tree
			// System.out.println("AVL Tree : Pre Order");
			bw = new BufferedWriter(new FileWriter(f1));
			avlt.inorder_walk(avlt.root, bw);
			bw.close();
			// System.out.println("AVL Tree : Post Order");
			bw = new BufferedWriter(new FileWriter(f2));
			avlt.postorder_walk(avlt.root, bw);
			bw.close();

			// inorder walks of AVLHash Tree
			for (int i = 1; i <= avlh.sizeofhash; i++) {
				if (avlh.a[i] != null) {
					// System.out.println("Tree " + i + " Inorder");
					// System.out.print("Inorder of Tree " + i + ": ");
					bw = new BufferedWriter(new FileWriter(f3, true));
					// bw.write("Tree " + i + " Inorder");
					avlh.a[i].inorder_walk(avlh.a[i].root, bw);
					bw.newLine();
					bw.newLine();
					bw.close();
				} else {
					// System.out.println(Tree is empty at " + i);
				}
			}

			// BTree Write Sorted output to file
			bw = new BufferedWriter(new FileWriter(f4));
			bt.walk_through(bt.root, bw);
			bw.close();

			// BTree Level order output to file
			bw = new BufferedWriter(new FileWriter(f5));
			bt.level_order(bt.root, bw);
			bw.close();

			// BTree Hash Level Order
			for (int i = 1; i <= bth.sizeofhash; i++) {
				if (bth.a[i] != null) {
					bw = new BufferedWriter(new FileWriter(f6, true));
					bth.a[i].level_order(bth.a[i].root, bw);
					bw.newLine();
					bw.newLine();
					bw.close();
				} else {
					// System.out.println(Tree is empty at " + i);
				}
			}
			// System.out.println();
			System.out
					.println("Program Executed and the files are created in the current directory");
		}
		// If the Input arguments are not passed according to the excepted ones
		else {
			System.err.println("Incorrect Arguments Passed.");
			System.err
					.println("Arguments for random mode: -r <hashsize> <btree_order>");
			System.err.println("Arguments for UserInput mode: -u <filename>");
		}
	}
}
