import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class Globals {
	int NumberOfPreferredNeighbors;
	int UnchokingInterval;
	int OptimisticUnchokingInterval;
	String FileName;
	long FileSize;
	int PieceSize;
	long numberOfPieces;
	int[] peerId;
	String[] hostId;
	int[] portNumber;
	int[] hasFile;
	String[] messageType = { "choke", "unchoke", "intersted",
			"notinterested", "have", "bitfield", "request", "piece" };
	int mypeerid;
	String host_name[];
	int peer_port_temp; 
	String host_name_temp;
	
	int[] toPeers;
	
	ArrayList<Socket> socks;
	Thread[] recThreads;
	Thread[] sendThreads;
	SendNml[] sendClassObjs;
	
	//private HashMap<Integer,int[]> bitFields;
	private int[][] bitFields;
	private int[] myBitField;
	
	private int[] sendOption;
	private int[] beenChoked;
	private int[] haveChoked;
	private int[] interestStatus;
	private int[] downSpeed;
	int[] optUnchNeighbor;
	private int[] prefNeighbor;
	private int[] reqByPiece;
	private int[] reqForPiece;
	Thread oun;
	Thread pt;
	ArrayList<Integer> peersIds;
	public Globals() {
		//client= new ArrayList<Socket>(); //socket created when peer connects as a client
		//server= new ArrayList<Socket>(); //socket created when peer acts as a server
		socks = new ArrayList<Socket>(); 
		//peerRecNml= new ArrayList<Thread>(); //thread created on peer side for receiving normal messages
		//peerSendNml= new ArrayList<Thread>(); //thread created on peer side for sending normal messages
		//sendObjs = new ArrayList<SendNml>();
		
		//bitFields = new HashMap<Integer,int[]>();		
		//myBitField=new ArrayList<Integer>();
		
//		sendOption = new ArrayList<Integer>();
//		beenChoked = new ArrayList<Integer>();
//		haveChoked = new ArrayList<Integer>();
//		interestStatus = new ArrayList<Integer>();
		
		peersIds = new ArrayList<Integer>();
	}
	public synchronized void modRecThreads(Thread t,int index){
		recThreads[index] = t;
	}
	public synchronized void initRecThreads(int length){
		recThreads = new Thread[length];
	}
	public synchronized Thread getRecThreads(int index){
		return recThreads[index];
	}
	
	public synchronized void modSendThreads(Thread t,int index){
		sendThreads[index] = t;
	}
	public synchronized void initSendThreads(int length){
		sendThreads = new Thread[length];
	}
	public synchronized Thread getSendThreads(int index){
		return sendThreads[index];
	}
	
	public synchronized void modSendClassObjs(SendNml send,int index){
		sendClassObjs[index] = send;
	}
	public synchronized void initSendClassObjs(int length){
		sendClassObjs = new SendNml[length];
	}
	public synchronized SendNml getSendClassObjs(int index){
		return sendClassObjs[index];
	}
	
	public synchronized void initBitFields(int length){
		bitFields = new int[length][];
	}
	public synchronized void modBitFields(int threadNum,int piece,int pieceStatus){
		int[] temp = this.getBitFields(threadNum);
		temp[piece]=pieceStatus;
		//bitFields.put(threadNum, temp);
		this.addBitFields(threadNum,temp);
	}
	public synchronized int[] getBitFields(int num){
		return bitFields[num];
	}
	public synchronized void addBitFields(int threadNum,int[] BF){
		bitFields[threadNum]=BF;
	}
//	public synchronized void modBitFields(int threadNum,int piece,int pieceStatus){
//		int[] temp = this.getBitFields(threadNum);
//		temp[piece]=pieceStatus;
//		bitFields.put(threadNum, temp);
//	}
//	public synchronized int[] getBitFields(int num){
//		return bitFields.get(num);
//	}
//	public synchronized void addBitFields(int threadNum,int[] BF){
//		bitFields.put(threadNum, BF);
//	}
	
	public synchronized void createMyBitField(int[] input){
		for(int i=0;i<input.length;i++){
			myBitField[i]=input[i];
		}
	}
	public synchronized void modMyBitField(int piece,int pieceStatus){
		myBitField[piece]=pieceStatus;
	}
	public synchronized int[] getMyBitField(){
		return myBitField;
	}
	public synchronized void initMyBitField(int length){
		myBitField = new int[length];
	}
	
	public synchronized void modToPeers(int threadNum,int peerId){
		toPeers[threadNum]=peerId;
	}
	public synchronized int getToPeers(int threadNum){
		return toPeers[threadNum];
	}
	public synchronized void initToPeers(int length){
		toPeers = new int[length];
	}
	
	
	public synchronized void modBeenChoked(int threadNum,int chokeStatus){
		beenChoked[threadNum]=chokeStatus;
	}
	public synchronized void initBeenChoked(int length){
		beenChoked = new int[length];
		for (int i=0;i<length;i++) beenChoked[i] = 1;
	}
	
	public synchronized void modHaveChoked(int threadNum,int chokeStatus){
		haveChoked[threadNum]=chokeStatus;
	}
	public synchronized void initHaveChoked(int length){
		haveChoked = new int[length];
		for (int i=0;i<length;i++) haveChoked[i] = 1;
	}
	public synchronized int getHaveChoked(int index){
		return haveChoked[index];
	}
	
	public synchronized void modInterestStatus(int threadNum,int status){
		interestStatus[threadNum]=status;
	}
	public synchronized void initInterestStatus(int length){
		interestStatus = new int[length];
	}
	public synchronized int getInterestStatus(int index){
		return interestStatus[index];
	}
	
	public synchronized void initSendOption(int length){
		sendOption = new int[length];
	}
	
	
	public synchronized void initDownSpeed(int length){
		downSpeed = new int[length];
	}
	public synchronized int getDownSpeed(int index){
		return downSpeed[index];
	}
	public synchronized void modDownSpeed(int threadNum){
		downSpeed[threadNum]++;
	}
	
	public synchronized void initOptUnchNeighbor(int length){
		optUnchNeighbor = new int[length];
	}
	public synchronized int getOptUnchNeighbor(int index){
		return optUnchNeighbor[index];
	}
	public synchronized void modOptUnchNeighbor(int threadNum){
		//optUnchNeighbor[threadNum]++;
		if (optUnchNeighbor[threadNum]==0) optUnchNeighbor[threadNum]=1;
		else optUnchNeighbor[threadNum]=0;
	}

	
	public synchronized void initPrefNeighbor(int length){
		prefNeighbor = new int[length];
	}
	public synchronized int getPrefNeighbor(int index){
		return prefNeighbor[index];
	}
	public synchronized void modPrefNeighbor(int threadNum,int status){
		prefNeighbor[threadNum]=status;
	}
	
	public synchronized int getNumOfInterestedPeers(){
		int count=0;
		for (int i : interestStatus){
			if (i==1) count++;
		}
		return count;
	}
	
	public synchronized void initReqByPiece(int length){
		reqByPiece = new int[length];
	}
	public synchronized int getReqByPiece(int index){
		return reqByPiece[index];
	}
	public synchronized void modReqByPiece(int threadNum,int pieceNum){
		reqByPiece[threadNum]=pieceNum;
	}

	public synchronized void initReqForPiece(int length){
		reqForPiece = new int[length];
	}
	public synchronized int getReqForPiece(int index){
		return reqForPiece[index];
	}
	public synchronized void modReqForPiece(int threadNum,int pieceNum){
		reqForPiece[threadNum]=pieceNum;
	}
}
