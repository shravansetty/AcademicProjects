import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

// CNT5106C - Project: P2P file sharing software similar to BitTorrent
//Project partners: Shravan Pentamsetty - 98625845 && Nyshadh Reddy Rachamallu - 19113564
//&& Padigender Reddy Kyasaram 11237229

public class peerProcess {
//	static int NumberOfPreferredNeighbors;
//	static int UnchokingInterval;
//	static int OptimisticUnchokingInterval;
//	static String FileName;
//	static long FileSize;
//	static int PieceSize;
	static long numberOfPieces;
//	static int[] peerId;
//	static String[] hostId;
//	static int[] portNumber;
//	static int[] hasFile;
//	static String[] messageType = { "choke", "unchoke", "intersted",
//			"notinterested", "have", "bitfield", "request", "piece" };
	static int mypeerid;
//	static String host_name[];
//	
//	static int peer_port_temp; 
//	static String host_name_temp;
//	
//	static ArrayList<Socket> client= new ArrayList<Socket>(); //socket created when peer connects as a client
//	static ArrayList<Socket> server= new ArrayList<Socket>(); //socket created when peer acts as a server
//	static ArrayList<Thread> peerRecNml= new ArrayList<Thread>(); //thread created on peer side for receiving normal messages
//	static ArrayList<Thread> peerSendNml= new ArrayList<Thread>(); //thread created on peer side for sending normal messages
//	static ArrayList<SendNml> sendObjs = new ArrayList<SendNml>(); 
//	static ArrayList<ArrayList<Integer>> bitFields = new ArrayList<ArrayList<Integer>>();		
//	static ArrayList<Integer> myBitField;
//	static ArrayList<Integer> sendOption = new ArrayList<Integer>();
//	static ArrayList<Integer> beenChoked = new ArrayList<Integer>();
//	static ArrayList<Integer> haveChoked = new ArrayList<Integer>();
//	static ArrayList<Integer> interestStatus = new ArrayList<Integer>();
	
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		Globals globals = new Globals();
		try {
			globals.mypeerid = Integer.parseInt(args[0]);
			mypeerid = globals.mypeerid; 
			// System.out.println(mypeerid);
		} catch (Exception ex) {
			System.out.println("Incorrect Argument: peerid");
			ex.printStackTrace();
		}
		// calling a method to read config files
		readConfigFiles(globals);
		// Check if the file exists,if hasFile is set to 1 for this peerid
		if (globals.mypeerid==globals.peerId[0]){
		if (!fileExists(globals)&&globals.mypeerid==globals.peerId[0]) {
			System.out.println("hasFile is set to 1 but the file does not exist for "+ globals.mypeerid);
			System.exit(1);
		}else{
			// divide the file into chunks
			divideFile(globals);
		}}
		 
		int[] tempBF= Bitfield.create(globals.mypeerid);
		//System.out.println(tempBF.length);
		globals.initMyBitField(tempBF.length);
		globals.createMyBitField(tempBF);
		//intialization
		globals.initBeenChoked(globals.peerId.length-1); 
		globals.initHaveChoked(globals.peerId.length-1); 
		globals.initInterestStatus(globals.peerId.length-1);
		globals.initSendOption(globals.peerId.length-1);
		globals.initToPeers(globals.peerId.length-1);
		globals.initDownSpeed(globals.peerId.length-1);
		globals.initOptUnchNeighbor(globals.peerId.length-1);
		globals.initPrefNeighbor(globals.peerId.length-1);
		globals.initSendClassObjs(globals.peerId.length-1);
		globals.initSendThreads(globals.peerId.length-1);
		globals.initRecThreads(globals.peerId.length-1);
		globals.initReqByPiece(globals.peerId.length-1);
		globals.initReqForPiece(globals.peerId.length-1);
		globals.initBitFields(globals.peerId.length-1);
		//
		
//		byte[] bf = ActualMessage.prepareWOpayLoad("bitfield");
//		ReceiveNml test = new ReceiveNml(null,1002,6,globals);
//		int[] test1=test.test(bf);
		
		
		int myPort=0,i,threadNum=0;
		for(i=0;i<globals.peerId.length;i++){
			if (globals.mypeerid==globals.peerId[i]){
				myPort = globals.portNumber[i];
				break;
			}
			Socket s = new Socket(globals.hostId[i], globals.portNumber[i]);
			globals.socks.add(threadNum,s);
			Log.prepTCPlogMake(globals.mypeerid, globals.peerId[i]);
			globals.peersIds.add(threadNum,globals.peerId[i]);
			globals.modToPeers(threadNum, globals.peerId[i]);
			//System.out.println("coonected to :" + globals.getToPeers(i));
			globals.modHaveChoked(threadNum, 1);
			//SendNml sendNml = new SendNml(s,globals.peerId[i],threadNum,globals);
			//Thread t2=new Thread(sendNml);
			//globals.peerSendNml.add(t2);
			//globals.sendObjs.add(threadNum,sendNml);
			sendFunc.doit(globals, 8, Handshake.prepare(globals.mypeerid), threadNum, s,globals.peerId[i]);
			
			ReceiveNml recNml = new ReceiveNml(s,globals.peerId[i],threadNum,globals);
			Thread t1=new Thread(recNml);
			globals.modRecThreads(t1,threadNum);
			
			t1.start();
			//t2.start();
			threadNum++;
		}
		int expConn = globals.peerId.length-i-1;
		i++; //to exclude port number of myPeer
		if (expConn>0){
			//System.out.println(myPort);//remove
			ServerSocket serSock = new ServerSocket(myPort);
			//System.out.println("ccpeted");//remove
			while(expConn>0){
				
				Socket s=serSock.accept(); 
				globals.socks.add(threadNum,s);
				Log.prepTCPlogMade(globals.mypeerid, globals.peerId[i]);
				globals.peersIds.add(threadNum,globals.peerId[i]);
				globals.modHaveChoked(threadNum, 1);
				globals.modToPeers(threadNum, globals.peerId[i]);
				
//				SendNml sendNml = new SendNml(s,globals.peerId[i], threadNum,globals);
//				Thread t2=new Thread(sendNml);
//				globals.peerSendNml.add(t2);
//				globals.sendObjs.add(threadNum,sendNml);
				sendFunc.doit(globals, 8, Handshake.prepare(globals.mypeerid), threadNum, s,globals.peerId[i]);
				
				ReceiveNml recNml = new ReceiveNml(s,globals.peerId[i],threadNum,globals);
				Thread t1=new Thread(recNml);
				globals.modRecThreads(t1,threadNum);
				
				t1.start();
				//t2.start();

				expConn--;
				threadNum++;
				i++;
			}
			serSock.close();
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			for(i=0;i<globals.peerRecNml.size();i++){
//				Thread temp=globals.peerRecNml.get(i);
//				if (temp!=null) temp.stop();
//			}
//			for(i=0;i<globals.peerSendNml.size();i++){
//				Thread temp=globals.peerSendNml.get(i);
//				if (temp!=null) temp.stop();
//			}
		}
		PrefNeighbors test2 = new PrefNeighbors(globals);
		Thread x = new Thread(test2);
		x.start();
		globals.pt=x;
//		try {
//			x.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		OptUnchNeighbor oun = new OptUnchNeighbor(globals);
		Thread y = new Thread(oun);
		y.start();
		globals.oun=y;
		//try {
		//	x.join();
		//} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		Stop st = new Stop(globals);
		Thread z = new Thread(st);
		z.start();
		try {
			z.join();
		} catch (InterruptedException e) {
		 //TODO Auto-generated catch block
			e.printStackTrace();
		}
		//peerProcess.stitchFile((int) globals.numberOfPieces, globals.mypeerid);
	}


	public static void readConfigFiles(Globals globals) {
		File myFile;
		FileReader fr;
		BufferedReader br;
		String line;
		String[] values;
		// Read Common.cfg file and update the parameters in the program
		try {
			myFile = new File("Common.cfg");
			fr = new FileReader(myFile);
			br = new BufferedReader(fr);
			line = null;
			values = null;
			line = br.readLine();
			values = line.split(" ");
			globals.NumberOfPreferredNeighbors = Integer.parseInt(values[1]);
			line = br.readLine();
			values = line.split(" ");
			globals.UnchokingInterval = Integer.parseInt(values[1]);
			line = br.readLine();
			values = line.split(" ");
			globals.OptimisticUnchokingInterval = Integer.parseInt(values[1]);
			line = br.readLine();
			values = line.split(" ");
			globals.FileName = values[1];
			line = br.readLine();
			values = line.split(" ");
			globals.FileSize = Integer.parseInt(values[1]);
			line = br.readLine();
			values = line.split(" ");
			globals.PieceSize = Integer.parseInt(values[1]);
			globals.numberOfPieces = (int) Math.ceil(globals.FileSize / (double) globals.PieceSize);
			numberOfPieces = globals.numberOfPieces; 
		} catch (Exception ex) {
			System.out.println("Exception while reading Common.cfg");
			ex.printStackTrace();
		}

		// Read PeerInfo.cfg and update peerid array
		try {
			myFile = new File("PeerInfo.cfg");
			fr = new FileReader(myFile);
			br = new BufferedReader(fr);
			int count = 0;
			while (br.readLine() != null) {
				count++;
			}
			globals.peerId = new int[count];
			globals.hostId = new String[count];
			globals.portNumber = new int[count];
			globals.hasFile = new int[count];
			// System.out.println(count);
			myFile = new File("PeerInfo.cfg");
			fr = new FileReader(myFile);
			br = new BufferedReader(fr);
			count = 0;
			do {
				line = br.readLine();
				if (line != null) {
					values = line.split(" ");
					globals.peerId[count] = Integer.parseInt(values[0]);
					globals.hostId[count] = values[1];
					globals.portNumber[count] = Integer.parseInt(values[2]);
					globals.hasFile[count] = Integer.parseInt(values[3]);
					count++;
				} else
					break;
			} while (true);
			// System.out.println(count);
		} catch (Exception ex) {
			System.out.println("Exception while reading file PeerInfo.cfg");
			ex.printStackTrace();
		}

	}

	// Code to create connection between all peers read so far from PeerInfo.cfg
//	public static void createTCPInitial(int index) {
//		for (int i = index; i >= 0; i--) {
//			if (!(i == index) && peerId[index] == mypeerid) {
//				createTCPConnection(peerId[index], peerId[i]);
//				System.out.println("Create TCP Connection between "
//						+ peerId[index] + " " + peerId[i]);
//			}
//		}
//	}

	// Code to create connection between two peers
	public static void createTCPConnection(int peer1, int peer2) {

	}

	// Divide the given file into pieces
	public static void divideFile(Globals globals) throws IOException {
		// System.out.println(new File("TheFile.dat").length());
		File f = new File(globals.FileName);
		BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(f));
		FileOutputStream out;
		// String name = f.getName();
		int fileIndex = 1;
		byte[] buffer = new byte[globals.PieceSize];
		int tmp = 0;
		File dir = new File("peer_" + globals.mypeerid);
		dir.mkdir();
		while ((tmp = bis.read(buffer)) > 0) {
			File newFile = new File(dir, String.format("%d", fileIndex++)
					+ ".dat");
			newFile.createNewFile();
			out = new FileOutputStream(newFile);
			out.write(buffer, 0, tmp);
			out.close();
		}
	}

	// check if the file actually exists, if the hasFile is set to 1 in
	// PeerInfo.cfg
	public static boolean fileExists(Globals globals) {
		for (int i = 0; i < globals.peerId.length; i++) {
			if (globals.peerId[i] == globals.mypeerid && globals.hasFile[i] == 1) {
				// if file exists - return true else return false;
				if (new File(globals.FileName).exists())
					return true;
				else
					return false;
			}
		}
		return false;
	}
	
	public static int getportnum()
	{
		//function to return random port num
		return 10000;
	}

			public static void stitchFile(int x, int peerid) {
				File f1=null;
				BufferedWriter bw=null;
				f1 = new File("TheFile" + peerid + ".dat");
				f1.delete();
				try {
					f1.createNewFile();
					bw = new BufferedWriter(new FileWriter(f1));
				} catch (Exception e) {
					e.printStackTrace();
				}

				for (int i = 1; i <= x; i++) {
					File myFile = new File("\\peer_"+peerid+"\\"+i + ".dat");
					FileReader fileReader;
					try {
						fileReader = new FileReader(myFile);
						BufferedReader reader = new BufferedReader(fileReader);
						String line = null;
						while ((line = reader.readLine()) != null) {
							bw.write(line);
							bw.newLine();
						}
						fileReader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		

	}



