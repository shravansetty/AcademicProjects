import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class arm {
	static File f1, f2, f3, f4;
	static String uname, pwd;
	static float t1_support, t2_support, t3_support, t4_support;
	static int t3_size, t4_size;
	static float t4_confidence;
	static String[] items_list = new String[90];

	// Method to create the required output files
	private static void createfiles() {
		f1 = new File("system.out.1");
		f2 = new File("system.out.2");
		f3 = new File("system.out.3");
		f4 = new File("system.out.4");
		// Delete all the existing files
		f1.delete();
		f2.delete();
		f3.delete();
		f4.delete();
		// Creating New Files
		try {
			f1.createNewFile();
			f2.createNewFile();
			f3.createNewFile();
			f4.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Method to read dat file and insert the corresponding records into table
	private static void readfile(String filename, String tablename)
			throws NumberFormatException, IOException {
		int i = 0;
		File myFile = new File(filename);
		FileReader fileReader;
		try {
			Connection Myconn = DriverManager.getConnection(
					"jdbc:oracle:thin:@oracle.cise.ufl.edu:1521:orcl", uname,
					pwd);
			Statement stmt = Myconn.createStatement();
			fileReader = new FileReader(myFile);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = null;
			String delim = ",";
			String s1 = null;
			String s2 = null;
			System.out.println("Inserting records into table " + tablename);
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(delim);
				s1 = values[0];
				s2 = values[1];
				if (tablename == "items") {
					items_list[i] = s2.replace("'", "");
					i++;
				}
				// System.out.println(s1 + " " + s2);
				String ps = "insert into " + tablename + " values(" + s1 + ","
						+ s2 + ")";
				// System.out.println(ps);
				stmt.executeUpdate(ps);
			}
			stmt.close();
			Myconn.close();
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to read the input parameters to appropriate variables from
	// system.in file
	private static void read_in() {
		File myFile = new File("system.in");
		FileReader fileReader;
		String[] values;
		try {
			fileReader = new FileReader(myFile);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = null;
			line = reader.readLine();
			values = line.split("[ =,\"]+");
			uname = values[1];
			pwd = values[3];
			line = reader.readLine();
			values = line.split("[ =%]+");
			t1_support = Float.parseFloat(values[2]);
			line = reader.readLine();
			values = line.split("[ =%]+");
			t2_support = Float.parseFloat(values[2]);
			line = reader.readLine();
			values = line.split("[ =%]+");
			t3_support = Float.parseFloat(values[2]);
			t3_size = Integer.parseInt(values[4]);
			line = reader.readLine();
			values = line.split("[ =%]+");
			t4_support = Float.parseFloat(values[2]);
			t4_confidence = Float.parseFloat(values[4]);
			t4_size = Integer.parseInt(values[6]);
			reader.close();
			System.out.println("UserName: " + uname + "; Password: " + pwd);
			System.out.println("Support for Task1: " + t1_support);
			System.out.println("Support for Task2: " + t2_support);
			System.out.println("Support for Task3: " + t3_support
					+ "; Size for Task3: " + t3_size);
			System.out.println("Support for Task4: " + t4_support
					+ "; Confidence for Task4: " + t4_confidence
					+ "; Size for Task4: " + t4_size);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to execute task1 of the project
	private static void task1(Connection Myconn) throws SQLException,
			IOException {
		// Create a Statement
		Statement stmt = Myconn.createStatement();
		try {
			// Task 1
			String support = null;
			String support1 = null;
			int count = 0;
			ResultSet rset0 = stmt
					.executeQuery("select count(distinct transid)  from trans");
			if (rset0.next()) {
				count = rset0.getInt(1);
				support = String.valueOf(Math.ceil(count * t1_support / 100.0));
				// System.out.println(count);
				// System.out.println(support);
			} else {
				System.out.println("Zero rows in trans table");
				System.exit(1);
			}
			stmt.executeUpdate("CREATE OR REPLACE VIEW TASK1_VW AS SELECT * FROM TRANS WHERE itemid IN( SELECT ITEMID FROM TRANS GROUP BY ITEMID HAVING COUNT(*) >="
					+ support + ")");
			ResultSet rset = stmt
					.executeQuery("SELECT TASK1_VW.ITEMID,ITEMS.ITEMNAME,COUNT(TASK1_VW.ITEMID) CNT FROM TASK1_VW,ITEMS WHERE ITEMS.ITEMID=TASK1_VW.ITEMID  GROUP BY TASK1_VW.ITEMID,ITEMS.ITEMNAME ORDER BY CNT");
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"system.out.1"));
			// Iterate through the result and print the employee names
			while (rset.next()) {
				support1 = String
						.valueOf(((float) (rset.getInt(3) * 100) / (float) count));
				bw.write("{" + rset.getString(2) + "},s=");
				bw.write(support1 + "%");
				bw.newLine();
			}
			stmt.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to execute task2 of the project
	private static void task2(Connection Myconn) throws SQLException,
			IOException {
		// Create a Statement
		Statement stmt = Myconn.createStatement();
		try {
			// Task 2
			String support = null;
			String support1 = null;
			int count = 0;
			ResultSet rset0 = stmt
					.executeQuery("select count(distinct transid)  from trans");
			if (rset0.next()) {
				count = rset0.getInt(1);
				support = String.valueOf(Math.ceil(count * t2_support / 100.0));
				// System.out.println("t2: count" + count);
				// System.out.println("t2 support: " + support);
			} else {
				System.out.println("Zero rows in trans table");
				System.exit(1);
			}
			stmt.executeUpdate("CREATE OR REPLACE VIEW TASK2_VW1 AS SELECT * FROM TRANS WHERE itemid IN( SELECT ITEMID FROM TRANS GROUP BY ITEMID HAVING COUNT(*) >="
					+ support + ")");
			ResultSet rset1 = stmt
					.executeQuery("SELECT A.ITEMID,ITEMS.ITEMNAME,COUNT(A.ITEMID) CNT FROM TASK2_VW1 A,ITEMS WHERE ITEMS.ITEMID=A.ITEMID  GROUP BY A.ITEMID,ITEMS.ITEMNAME ORDER BY CNT");
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"system.out.2"));
			// Iterate through the result and print the employee names
			while (rset1.next()) {

				bw.write("{" + rset1.getString(2) + "},s=");
				support1 = String
						.valueOf(((float) (rset1.getInt(3) * 100) / (float) count));
				bw.write(support1 + "%");
				bw.newLine();
			}
			stmt.executeUpdate("CREATE OR REPLACE VIEW TASK2_VW2 AS SELECT * FROM TRANS WHERE itemid IN( SELECT ITEMID FROM TRANS GROUP BY ITEMID HAVING COUNT(*) >="
					+ support + ")");
			ResultSet rset = stmt
					.executeQuery("select B1.ITEMID, B2.ITEMID,I1.ITEMNAME,I2.ITEMNAME,COUNT(*) CNT from TASK2_VW2 B1, TASK2_VW2 B2,ITEMS I1,ITEMS I2 where B1.TRANSID = B2.TRANSID and B1.ITEMID < B2.ITEMID and B1.ITEMID = I1.ITEMID and B2.ITEMID = I2.ITEMID group by B1.ITEMID, B2.ITEMID,I1.ITEMNAME,I2.ITEMNAME having COUNT(*) >="
							+ support + "ORDER BY CNT");
			// Iterate through the result and print the employee names
			while (rset.next()) {
				bw.write("{" + rset.getString(3) + ", " + rset.getString(4)
						+ "},s=");
				// System.out.println(rset.getInt(5) + "/" + count);
				support1 = String
						.valueOf(((float) (rset.getInt(5) * 100) / (float) count));
				bw.write(support1 + "%");
				bw.newLine();
			}
			stmt.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to execute task3 of the project
	private static void task3(Connection Myconn) throws SQLException,
			IOException {
		// Create a Statement
		Statement stmt = Myconn.createStatement();
		String support = null;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"system.out.3"));
			int count = 0;
			ResultSet rset0 = stmt
					.executeQuery("select count(distinct transid)  from trans");
			if (rset0.next()) {
				count = rset0.getInt(1);
				support = String.valueOf(Math.ceil(count * t3_support / 100.0));
				// System.out.println("t3: count" + count);
				// System.out.println("t3 support: " + support);
			} else {
				System.out.println("Zero rows in trans table");
				System.exit(1);
			}
			create_cand_freq_tables(t3_size, Myconn);
			stmt.executeQuery("insert into FISET_1 SELECT ITEMID, COUNT(*) FROM trans GROUP BY ITEMID HAVING COUNT(*)>="
					+ support);
			insert_cand_freq_tables(t3_size, support, Myconn);
			// Print the output to text file
			for (int p = 1; p <= t3_size; p++) {
				// System.out.println("Select * from fiset_" + p);
				rset0 = stmt.executeQuery("Select * from fiset_" + p
						+ " order by count");
				String temp = "";
				while (rset0.next()) {
					int q = 1;
					int id = 0;
					temp = "";
					while (q <= p) {
						if (q < p) {
							id = rset0.getInt(q);
							temp = temp + items_list[id] + ", ";
							q = q + 1;
						} else {
							id = rset0.getInt(q);
							temp = temp + items_list[id];
							q = q + 1;
						}
					}
					// System.out.println("{" + temp + "},s=");
					bw.write("{" + temp + "},s=");
					String support1 = String.valueOf(((float) (rset0
							.getInt(p + 1) * 100) / (float) count));
					bw.write(support1 + "%");
					bw.newLine();
				}
			}
			bw.close();
			stmt.close();
			// ResultSet rs = cs.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to execute task4 of the project
	private static void task4(Connection Myconn) throws SQLException,
			IOException {
		// Create a Statement
		Statement stmt = Myconn.createStatement();
		String support = null;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"system.out.4"));
			int count = 0;
			ResultSet rset0 = stmt
					.executeQuery("select count(distinct transid)  from trans");
			if (rset0.next()) {
				count = rset0.getInt(1);
				support = String.valueOf(Math.ceil(count * t4_support / 100.0));
				// System.out.println("t4: count" + count);
				// System.out.println("t4 support: " + support);
			} else {
				System.out.println("Zero rows in trans table");
				System.exit(1);
			}
			if (!(t3_support == t4_support && t3_size > t4_size)) {
				create_cand_freq_tables(t4_size, Myconn);
				stmt.executeQuery("insert into FISET_1 SELECT ITEMID, COUNT(*) FROM trans GROUP BY ITEMID HAVING COUNT(*)>="
						+ support);
				insert_cand_freq_tables(t4_size, support, Myconn);
			} else {
				// System.out
				// .println("Can find rules from tables created in task3");
			}

			for (int i = 2; i <= t4_size; i++) {
				// System.out.println("Test");
				int items[] = new int[i];
				int count_sup = 0;
				int cnt_rules = 0;
				ResultSet rs = stmt.executeQuery("SELECT * FROM FISET_" + i);
				while (rs.next()) {
					for (int j = 1; j <= i; j++) {
						items[j - 1] = rs.getInt(j);
					}
					count_sup = rs.getInt(i + 1);
					// System.out.println(count_sup);
					ArrayList<Integer> set = new ArrayList<Integer>();
					ArrayList<Integer> FullSet = new ArrayList<Integer>();
					for (int n : items) {
						if (!set.contains(n)) {
							set.add(n);
						}
					}
					ArrayList<ArrayList<Integer>> subSets = getSubsets(set);
					FullSet = subSets.get(subSets.size() - 1);
					subSets.remove(0);
					subSets.remove(subSets.size() - 1);

					for (int p = 0; p < subSets.size(); p++) {
						ArrayList<Integer> left = new ArrayList<Integer>();
						ArrayList<Integer> right = new ArrayList<Integer>();
						int subsize = subSets.get(p).size();
						String sel_quer = "Select count from FISET_"
								+ (subsize) + " where ";
						for (int q = 0; q < subsize; q++) {
							// System.out.print(subSets.get(p).get(q) + "  ");
							if (q != subsize - 1) {
								sel_quer = sel_quer + " itemid" + (q + 1)
										+ " = " + subSets.get(p).get(q)
										+ " AND ";
								left.add(subSets.get(p).get(q));
							} else {
								sel_quer = sel_quer + " itemid" + (q + 1)
										+ " = " + subSets.get(p).get(q);
								left.add(subSets.get(p).get(q));
							}
						}
						// System.out.println(sel_quer);
						Statement stmt1 = Myconn.createStatement();
						ResultSet rsp = stmt1.executeQuery(sel_quer);
						rsp.next();
						int count_sup1 = rsp.getInt(1);
						rsp.close();
						float confidence = (float) count_sup / count_sup1;
						float t4_conf = (float) ((float) t4_confidence / 100.0);
						if (confidence >= t4_conf) {
							// System.out.println(sel_quer);
							// System.out.println(count_sup1);
							// System.out.println(confidence);
							for (int r = 0; r < FullSet.size(); r++) {
								if (subSets.get(p).contains(FullSet.get(r))) {
								} else {
									right.add(FullSet.get(r));
									// System.out.println("||" +
									// FullSet.get(r));
								}
							}
							// System.out.println(left.size());
							// System.out.println(right.size());
							// write left side of the rule
							bw.write("{{");
							for (int a = 0; a < left.size(); a++) {
								if (a == left.size() - 1) {
									bw.write(items_list[left.get(a)] + "} -> ");
									// bw.write(left.get(a) + "} -> ");
								} else {
									bw.write(items_list[left.get(a)] + ", ");
									// bw.write(left.get(a) + ", ");
								}
							}
							// write right side of the rule
							bw.write("{");
							for (int a = 0; a < right.size(); a++) {
								if (a == right.size() - 1) {
									bw.write(items_list[right.get(a)] + "}},");
									// bw.write(right.get(a) + "}},");
								} else {
									bw.write(items_list[right.get(a)] + ", ");
									// bw.write(right.get(a) + ", ");
								}
							}
							float supp_final = (float) (count_sup * 100.0 / count);
							// bw.write("s=" + count_sup + "%, ");
							// bw.write("s=" + count_sup1 + "%, ");
							cnt_rules = cnt_rules + 1;
							bw.write("s=" + supp_final + "%, ");
							bw.write("c=" + (confidence * 100) + "%");
							bw.newLine();
						}
						stmt1.close();
						// System.out.print();
						// System.out.println();
					}
				}
				System.out.println(cnt_rules + " rules of size " + i);
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to create Candidate and Frequent tables
	private static void create_cand_freq_tables(int size, Connection Myconn)
			throws SQLException {
		int i = size;
		int j = size;
		Statement stmt = Myconn.createStatement();
		ResultSet rset0;
		// Delete FI Tables
		while (i > 0) {
			String del_sql = "drop table fiset_";
			String num = String.valueOf(i);
			del_sql = del_sql + num;
			// System.out.println(del_sql);
			rset0 = stmt
					.executeQuery("Select count(1) from all_tables where table_name = 'FISET_"
							+ num + "'");
			rset0.next();
			if (rset0.getInt(1) != 0) {
				stmt.executeUpdate(del_sql);
			}
			i = i - 1;
		}
		// Delete Candidate Tables
		while (j > 1) {
			String del_sql = "drop table cset_";
			String num = String.valueOf(j);
			del_sql = del_sql + num;
			// System.out.println(del_sql);
			rset0 = stmt
					.executeQuery("Select count(1) from all_tables where table_name = 'CSET_"
							+ num + "'");
			rset0.next();
			if (rset0.getInt(1) != 0) {
				stmt.executeUpdate(del_sql);
			}
			j = j - 1;
		}

		// Create Candidate Tables
		int k = 2;
		String create_sql = null;
		int l = 1;
		while (k <= size) {
			l = 1;
			create_sql = "create table cset_";
			create_sql = create_sql + String.valueOf(k) + "(";
			while (l < k) {
				create_sql = create_sql + "itemid" + String.valueOf(l)
						+ " number,";
				l = l + 1;
			}
			create_sql = create_sql + "itemid" + String.valueOf(l) + " number)";
			// System.out.println(create_sql);
			stmt.executeUpdate(create_sql);
			k = k + 1;
		}

		// Create FI Tables
		int a = 1;
		create_sql = null;
		int b = 1;
		while (a <= size) {
			b = 1;
			create_sql = "create table FISet_";
			create_sql = create_sql + String.valueOf(a) + "(";
			while (b <= a) {
				create_sql = create_sql + "itemid" + String.valueOf(b)
						+ " number,";
				b = b + 1;
			}
			create_sql = create_sql + "count number)";
			// stmt.executeUpdate(del_sql);
			// System.out.println(create_sql);
			stmt.executeUpdate(create_sql);
			a = a + 1;
		}
	}

	// Method to Insert into Candidate and Frequent tables
	private static void insert_cand_freq_tables(int size, String support,
			Connection Myconn) throws SQLException {
		CallableStatement cs_cand = null;
		CallableStatement cs_fi = null;
		for (int z = 2; z <= size; z++) {
			// Generate Candidate Frequent item Sets
			cs_cand = Myconn.prepareCall("{call CANDIDATE_SETS(?)}");
			cs_cand.setInt(1, z);
			cs_cand.execute();
			// Prune the above generated set to get only Valid Frequent Item
			// Sets
			cs_fi = Myconn.prepareCall("{call FREQUENT_ITEM_SETS(?,?)}");
			cs_fi.setInt(1, z);
			int sup = (int) Float.parseFloat(support);
			cs_fi.setInt(2, sup);
			cs_fi.execute();
		}
	}

	private static ArrayList<ArrayList<Integer>> getSubsets(
			ArrayList<Integer> set) {
		ArrayList<ArrayList<Integer>> subsetCollection = new ArrayList<ArrayList<Integer>>();
		if (set.size() == 0) {
			subsetCollection.add(new ArrayList<Integer>());
		} else {
			ArrayList<Integer> reducedSet = new ArrayList<Integer>();
			reducedSet.addAll(set);
			int first = reducedSet.remove(0);
			ArrayList<ArrayList<Integer>> subsets = getSubsets(reducedSet);
			subsetCollection.addAll(subsets);
			subsets = getSubsets(reducedSet);
			for (ArrayList<Integer> subset : subsets) {
				subset.add(0, first);
			}
			subsetCollection.addAll(subsets);
		}
		return subsetCollection;
	}

	// Entry point of the program
	public static void main(String[] args) throws SQLException, IOException,
			InterruptedException {
		try {
			long start = 0, stop = 0;
			long time = 0;
			start = System.currentTimeMillis();
			read_in();
			createfiles();
			String cmdline = "sqlplus " + uname + "@orcl/" + pwd + " @arm.sql";
			Process p = Runtime.getRuntime().exec(cmdline);
			p.waitFor();
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			Connection Myconn = DriverManager.getConnection(
					"jdbc:oracle:thin:@oracle.cise.ufl.edu:1521:orcl", uname,
					pwd);
			System.out.println("Got the Connection");
			Statement stmt = Myconn.createStatement();
			// calls the cleanup procedure
			stmt.executeUpdate("call cleanup()");
			readfile("items.dat", "items");
			readfile("trans.dat", "trans");

			System.out.println("Executing Task1");
			task1(Myconn);
			System.out.println("Task1 Completed");
			System.out.println("Executing Task2");
			task2(Myconn);
			System.out.println("Task2 Completed");
			System.out.println("Executing Task3");
			task3(Myconn);
			System.out.println("Task3 Completed");
			System.out.println("Executing Task4");
			task4(Myconn);
			System.out.println("Task4 Completed");
			Myconn.close(); // ** IMPORTANT : Close connections when done **
			System.out.println("Program Completed");
			stop = System.currentTimeMillis();
			time = stop - start;
			System.out.println("Running Time of the Program: " + time / 1000
					+ " seconds");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
