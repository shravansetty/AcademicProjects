import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {
	public static void write(int peerIdNum,String log){
		String peerId = Integer.toString(peerIdNum);
 		String fileName="/log_peer_" + peerId +".log";
 		String str="";
		try {
			str = new java.io.File(".").getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
 		str=str+fileName;
		try {
			PrintWriter outer;
			outer= new PrintWriter(new BufferedWriter(new FileWriter(str, true)));
		    outer.println(log);
		    outer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	public static void prepTCPlogMake(int peerid1, int peerid2){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " makes a coonection to peer " + 
						Integer.toString(peerid2);
		Log.write(peerid1, log);
	}
	public static void prepTCPlogMade(int peerid1, int peerid2){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " is connected from peer " + 
						Integer.toString(peerid2);
		Log.write(peerid1, log);
	}
	//Qn have to change this
	public static void prepChangePreNeigborLog(int[] peerid, int mypeerid){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(mypeerid)+ " has the preferred neighbors ";
		for(int i=0;i<peerid.length;i++){
			if (peerid[i]!=mypeerid){
				log =log + "," + Integer.toString(peerid[i]);
			}
		}
		Log.write(mypeerid, log);
	}
	public static void prepOptUnchokedLog(int peerid1, int peerid2){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " has the optimistically unchoked neighbor " + 
						Integer.toString(peerid2);
		Log.write(peerid1, log);
	}
	public static void prepUnchokeLog(int peerid1, int peerid2){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " is unchoked by " + 
						Integer.toString(peerid2);
		Log.write(peerid1, log);
	}
	public static void prepChokeLog(int peerid1, int peerid2){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " is choked by " + 
						Integer.toString(peerid2);
		Log.write(peerid1, log);
	}
	public static void prepHaveLog(int peerid1, int peerid2,int piece){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " received the have message from " + 
						Integer.toString(peerid2)+" for the piece "+Integer.toString(piece);
		Log.write(peerid1, log);
	}
	public static void prepInterestedLog(int peerid1, int peerid2){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " received interested message from " + 
						Integer.toString(peerid2);
		Log.write(peerid1, log);
	}
	public static void prepNotInterestedLog(int peerid1, int peerid2){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " received the not interested message from " + 
						Integer.toString(peerid2);
		Log.write(peerid1, log);
	}
	public static void prepPieceLog(int peerid1, int peerid2,int pieceNum, int numOfPieces){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " has downloaded the piece " + Integer.toString(pieceNum) + "from"+ 
						Integer.toString(peerid2)+" Now the number of pieces it has is "+numOfPieces;
		Log.write(peerid1, log);
	}
	public static void prepCompletedLog(int peerid1){
		String log;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		log=timeStamp+ ": Peer " + Integer.toString(peerid1)+ " has downloaded the complete file ";
		Log.write(peerid1, log);
	}
}
