import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReceiveNml implements Runnable{
	Socket sock;
	int toPeer;
	int threadNum;
	int recOption;
	SendNml sendThread;
	Globals globals;
	public ReceiveNml(Socket s,int toPeer1, int threadNum1, Globals recGlobals){
		sock=s;
		toPeer = toPeer1;
		threadNum = threadNum1;
		//sendThread = sendObj; 
		globals = recGlobals;
	}
	public void run(){
		InputStream in=null;
		DataInputStream dis=null;
		try {
			in = sock.getInputStream();
			dis = new DataInputStream(in);
			byte[] HSmsg=new byte[dis.readInt()];
			dis.readFully(HSmsg);
			////System.out.println("HSmsg read");
			Log.write(globals.mypeerid, "handshake message read");
			if (!Handshake.check(toPeer, HSmsg)) {
				//System.out.println("HSmsg error");
				return;
			}
			else {
				byte[] bitFldMsg = ActualMessage.prepareWOpayLoad("bitfield");//
				//updating mybit field for the first time
				byte[] bitfield = ActualMessage.retBitfieldPayload(bitFldMsg);
				////System.out.println("wtf"+(int) globals.numberOfPieces);
				int[] bitfieldArr = new int[(int) globals.numberOfPieces]; 
				//int byteNum=bitfield.length-1,k;
				//sendThread.modQueue(5, bitFldMsg);
				for(int i=0; i<(int) globals.numberOfPieces;i++){ 
					//int bitIndex = (i-1) % 8;
					int bitIndex = 7-i%8;
				    int byteIndex = (i) / 8;
				    //int bitMask = 1 << 7-bitIndex;
				    int bitMask = 1 << bitIndex;
//				    //System.out.println("wtfival"+i);
				    if ((bitfield[byteIndex] & bitMask) > 0){
				    	bitfieldArr[i]=1; 
				    }else bitfieldArr[i]=0;
				}
				globals.createMyBitField(bitfieldArr);
				sendFunc.doit(globals, 5, bitFldMsg, this.threadNum, this.sock, this.toPeer);
				////System.out.println("RT:queued bitfld msg");
				//Log.write(globals.mypeerid,"this si th jnlij: " + this.threadNum + " " + this.toPeer);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while(recOption!=1){
			int dwnlComp=0;
			try {
				int len = dis.readInt();
				byte[] msg = new byte[len];
				dis.readFully(msg);
				String type = ActualMessage.check(msg);
				int pieceNum=0;
				switch (msg[4]){
					case 0:
						////System.out.println("RT:received choke msg");
						//Log.write(globals.mypeerid, "received choke message from " + globals.getToPeers(threadNum));
						globals.modBeenChoked(threadNum,1);
						Log.prepChokeLog(globals.mypeerid, globals.peersIds.get(threadNum));
						
						int reqForPiece = globals.getReqForPiece(threadNum);
						if (reqForPiece!=0){
							int[] myBF = globals.getMyBitField();
							if (myBF[reqForPiece-1]==-1){
								//Log.write(globals.mypeerid,"getting into this place " + reqForPiece);
								globals.modMyBitField(reqForPiece-1, 0);
							}
						}
						break;
						//Qn (should i remove request messages already in queue from the LL in sendNml class)
					case 1:
						//System.out.println("RT:received unchoke msg");
						//Log.write(globals.mypeerid, "received unchoke message from " + globals.getToPeers(threadNum));
						globals.modBeenChoked(threadNum,0);
						Log.prepUnchokeLog(globals.mypeerid, globals.peersIds.get(threadNum));
						//comparing bitfields to select a piece num
						pieceNum=selectReqPiece.find(globals.getMyBitField(), globals.getBitFields(threadNum));
						//modify corresponding piece num in bitfield to -1
						if (pieceNum==-1){
							//Log.write(globals.mypeerid, "after being unchoked couldnt find a piece to request");
							globals.modReqForPiece(threadNum, 0);
							break;
						}
						globals.modMyBitField(pieceNum-1,-1);
						//
						globals.modReqForPiece(threadNum, pieceNum);
						byte[] reqMsg = ActualMessage.prepRequest(pieceNum);
						//sendThread.modQueue(4, reqMsg);
						sendFunc.doit(globals, 4, reqMsg, this.threadNum, this.sock, this.toPeer);
						//
						break;
						
					case 2:
						//System.out.println("RT:received interested msg");
						//Log.write(globals.mypeerid, "received interested message from " + globals.getToPeers(threadNum));
						globals.modInterestStatus(threadNum,1);
						Log.prepInterestedLog(globals.mypeerid, globals.peersIds.get(threadNum));
						break;
					case 3:
						//System.out.println("RT:received notInterested msg");
						//Log.write(globals.mypeerid, "received not interested message from " + globals.getToPeers(threadNum));
						////System.out.println(ActualMessage.check(msg));
						//for (int i=0;i<msg.length;i++){
						//	//System.out.println("wtf:" +msg[i]);
						//}
						globals.modInterestStatus(threadNum,0);
						Log.prepNotInterestedLog(globals.mypeerid, globals.peersIds.get(threadNum));
						if (ReceiveNml.chkDwnldComp(globals,threadNum)==true){
							dwnlComp=1;
							break;
						}
						break;
					case 4:
						//System.out.println("RT:received have msg");
						pieceNum = ActualMessage.retHavePayload(msg);
						Log.prepHaveLog(globals.mypeerid, globals.peersIds.get(threadNum),pieceNum);
						globals.modBitFields(threadNum, pieceNum-1, 1);
						if (ReceiveNml.chkDwnldComp(globals,threadNum)==true){
							dwnlComp=1;
							break;
						}
						int[] other=globals.getBitFields(threadNum);	
//						other[pieceNum]=1;
//						globals.bitFields.put(threadNum, other);
						//checking the bitfield and then sending either interested or not interested message
						if (ReceiveNml.interestedOrNot(globals.getMyBitField(), other)){
							byte[] interestedMsg = ActualMessage.prepareWOpayLoad("interested");
							//sendThread.modQueue(2, interestedMsg);
							sendFunc.doit(globals, 2, interestedMsg, this.threadNum, this.sock, this.toPeer);
							//System.out.println("RT:queued interested msg");
						}else{
							byte[] notInterestedMsg = ActualMessage.prepareWOpayLoad("not interested");
							//sendThread.modQueue(3, notInterestedMsg);
							sendFunc.doit(globals, 3, notInterestedMsg, this.threadNum, this.sock, this.toPeer);
							//System.out.println("RT:queued notInterested msg");
						}
						break;
						
					case 5:
						//System.out.println("RT:received bitfld msg");
						//Log.write(globals.mypeerid, "received bitfield message from " + globals.getToPeers(threadNum));
						byte[] recBitfield = ActualMessage.retBitfieldPayload(msg);
						int[] recBitfieldArr = new int[(int) globals.numberOfPieces]; 
						int byteNum=recBitfield.length-1,k;
						////System.out.println("error place" + byteNum);
						//for(int i=recBitfield.length*8;i>0;i--){ //iterating from right most bit
						//int count = 8-((int)globals.numberOfPieces%8);
						//for(int i=0;i<count;i++) recBitfield[recBitfield.length-1] = (byte) (recBitfield[recBitfield.length-1] /2); 
						//for(int i=(int) globals.numberOfPieces;i>0;i--){
						for(int i=0;i<(int) globals.numberOfPieces;i++){
//							k=recBitfield[byteNum]%2;
//							if (k==1) recBitfieldArr[i-1]=1;
//							else recBitfieldArr[i-1]=0;
//							recBitfield[byteNum] = (byte) (recBitfield[byteNum] /2);
//							if (i%8==0) byteNum = byteNum -1;
							int bitIndex = 7-i%8;
							//int bitIndex = (i-1) % 8;
						    int byteIndex = (i) / 8;
						    //int bitMask = 1 << 7-bitIndex;
						    int bitMask = 1 << bitIndex;
						    if ((recBitfield[byteIndex] & bitMask) > 0){
						    	recBitfieldArr[i]=1; 
						    }else recBitfieldArr[i]=0;
						}
						globals.addBitFields(threadNum,recBitfieldArr);
						String s= "received bitfield from " + this.toPeer + " in threadNum " + threadNum;
						for (int i=0;i<recBitfieldArr.length;i++) s= s + recBitfieldArr[i];
						//Log.write(globals.mypeerid, s);
						//test
						int[] tempa = globals.getBitFields(threadNum);
						String s1= "testing received bitfield from " + this.toPeer + " ";
						for (int i=0;i<recBitfieldArr.length;i++) s1= s1 + tempa[i];
						//Log.write(globals.mypeerid, s1);
						
						int[] test = globals.getMyBitField();
						//System.out.print("outside loop mine:");
						//for (int i=0;i<test.length;i++) System.out.print(test[i]);
						
						//checking the bitfield and then sending either interested or not interested message
						if (ReceiveNml.interestedOrNot(globals.getMyBitField(),recBitfieldArr)){
							byte[] interestedMsg = ActualMessage.prepareWOpayLoad("interested");
							//sendThread.modQueue(2, interestedMsg);
							sendFunc.doit(globals, 2, interestedMsg, this.threadNum, this.sock, this.toPeer);
							//System.out.println("RT:queued interested msg");
						}else{
							byte[] notInterestedMsg = ActualMessage.prepareWOpayLoad("not interested");
							//sendThread.modQueue(3, notInterestedMsg);
							sendFunc.doit(globals, 3, notInterestedMsg, this.threadNum, this.sock, this.toPeer);
							//System.out.println("RT:queued notInterested msg");
						}
						break;
					case 6:
						
						//if interested and not choked then send piece for the request
						if (globals.getInterestStatus(threadNum)==1&&globals.getHaveChoked(threadNum)==0){
							int reqPiece = ActualMessage.retRequestPayload(msg);
							//globals.modBitFields(threadNum, reqPiece-1, -1);
							//System.out.println("RT:received request msg for piece "+ reqPiece);
							//Log.prepRequestLog(globals.mypeerid, globals.peersIds.get(threadNum),reqPiece);//Qn shouldthis moved outside loop?
							byte[] fileBytes= ActualMessage.prepPiece(reqPiece);
							//sendThread.modQueue(5, fileBytes);
							sendFunc.doit(globals, 5, fileBytes, this.threadNum, this.sock, this.toPeer);
							//System.out.println("RT:queued piece msg for piecenum "+ reqPiece);
							//Log.write(globals.mypeerid, "RT:queued piece msg for piecenum "+ reqPiece);
						}	
						break;
						
					case 7:
						//System.out.println("RT:recieved piece msg");
						int size = msg.length - 5; //size of piece payload in bytes
						//pieceNum=ActualMessage.parsePicePayload(msg,globals.mypeerid);
						//if (size==globals.PieceSize||()){
						if (size==globals.PieceSize+4||(size==(globals.FileSize%32768)+4)){//&&pieceNum==globals.numberOfPieces)){	
							SendNml temp;
							
							pieceNum=ActualMessage.parsePicePayload(msg,globals.mypeerid);
							globals.modMyBitField(pieceNum-1,1);
							globals.modReqForPiece(threadNum, 0);
							
							int[] mine = globals.getMyBitField();
							int counter=0;
							for(int l=0;l<mine.length;l++){
								if (mine[l]==1) counter++;
							}
							Log.prepPieceLog(globals.mypeerid, toPeer, pieceNum, counter);//Qn change 369
							
							
							
							globals.modDownSpeed(threadNum);
							for(int i=0;i<globals.toPeers.length;i++){//if received piece completely send have to all peers
								//temp = globals.sendObjs.get(i);
								//if (temp!=null){
									byte[] haveMsg= ActualMessage.prepHave(pieceNum);
									//temp.modQueue(4, haveMsg);
									sendFunc.doit(globals, 4, haveMsg, i, globals.socks.get(i), globals.getToPeers(i));//changed i from this.threadNum
									//System.out.println("RT:queued have msg");
								//}
							}
							
//							if (ReceiveNml.chkDwnldComp(globals,threadNum)==true){
//								dwnlComp=1;
//								break;
//							}
							
							for(int i=0;i<globals.toPeers.length;i++){
								if (i==threadNum) continue;
								int newPiece = selectReqPiece.find(globals.getMyBitField(), globals.getBitFields(i));
								if (newPiece==-1){
									byte[] notInterestedMsg = ActualMessage.prepareWOpayLoad("not interested");
									sendFunc.doit(globals, 3, notInterestedMsg, i, globals.socks.get(i), globals.getToPeers(i));
									//System.out.println("RT:queued notInterested msg");
								}
							}
							//temp = globals.sendObjs.get(threadNum);//then sending the new request msg to the sender
							int newPiece = selectReqPiece.find(globals.getMyBitField(), globals.getBitFields(threadNum));
							if (newPiece == -1){
								byte[] notInterestedMsg = ActualMessage.prepareWOpayLoad("not interested");
								//sendThread.modQueue(3, notInterestedMsg);
								sendFunc.doit(globals, 3, notInterestedMsg, this.threadNum, this.sock, this.toPeer);
								//System.out.println("RT:queued notInterested msg");
								globals.modReqForPiece(threadNum, 0);
								break;
							}
							globals.modMyBitField(newPiece-1,-1);
							globals.modReqForPiece(threadNum, newPiece);
							reqMsg = ActualMessage.prepRequest(newPiece);
							//temp.modQueue(4, reqMsg);
							sendFunc.doit(globals, 4, reqMsg, this.threadNum, this.sock, this.toPeer);
							//System.out.println("RT:queued request msg for piece " + ActualMessage.retRequestPayload(reqMsg));
						}else{
							//Log.write(globals.mypeerid, "getting into check place " + globals.getReqForPiece(threadNum));
							int reqForPieceNum = globals.getReqForPiece(threadNum);
							if (reqForPieceNum!=0)globals.modMyBitField(reqForPieceNum-1,0);
						}
						break;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
//			if (dwnlComp==1){
//				try {
//					//System.out.println("RTTTTTTTTTTTTTT: closing thread " + threadNum);
//					Log.write(1001, "RTTTTTTTTTTTTTT: closing thread " + threadNum);
//					in.close();
//					dis.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				break;
//			}
		}
	} 
	public static boolean interestedOrNot(int[] my,int[] other){
		for(int i=0;i<my.length;i++){
			if (other[i]==1&&my[i]==0){
				return true;
			}
		}
		System.out.print("mine:");
		for(int i=0;i<my.length;i++){
			System.out.print(my[i]);
		}
		//System.out.println("");
		System.out.print("other:");
		for(int i=0;i<my.length;i++){
			System.out.print(other[i]);
		}
		return false;
	}
	public  int[] test(byte[] msg){
		byte[] recBitfield = ActualMessage.retBitfieldPayload(msg);
		int[] recBitfieldArr = new int[45]; 
		int byteNum=recBitfield.length-1,k;
		//for(int i=recBitfield.length*8;i>0;i--){ //iterating from right most bit
		//int count = 8-(46%8);
		//for(int i=0;i<count;i++) recBitfield[recBitfield.length-1] = (byte) (recBitfield[recBitfield.length-1] /2); 
		for(int i=(int) 45;i>0;i--){ 
			int bitIndex = (i-1) % 8;
		    int byteIndex = (i-1) / 8;
		    int bitMask = 1 << 7-bitIndex;
		    if ((recBitfield[byteIndex] & bitMask) > 0){
		    	recBitfieldArr[i-1]=1; 
		    }else recBitfieldArr[i-1]=0;
		}
		return recBitfieldArr;
	}
	public static boolean chkDwnldComp(Globals globals,int threadNum){
		int[] myBF = globals.getMyBitField();
		for (int i=0;i<myBF.length;i++){
			if (myBF[i]==0 || myBF[i]==-1){
				return false;
			}
		}
//		for (int i=0;i<globals.toPeers.length;i++){
//			int[] otherBF = globals.getBitFields(i);
//			for (int k=0;k<otherBF.length;k++){
//				if (otherBF[k]==0 | otherBF[k]==-1){
//					return false;
//				}
//			}
//		}
		int[] other = globals.getBitFields(threadNum);
		for (int i=0;i<other.length;i++){
			if (other[i]==0 || other[i]==-1){
				return false;
			}
		}
		return true;
	}
}
