import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;


public class SendNml implements Runnable{
	Socket sock;
	int toPeer;
	int threadNum;
	volatile int stopSend;
	Globals globals;
	private LinkedList<Integer> queue = new LinkedList<Integer>();
	private LinkedList<byte[]> msgQueue = new LinkedList<byte[]>();
	public SendNml(Socket s,int toPeer1, int threadNum1,Globals recGlobals){
		sock=s;
		toPeer = toPeer1;
		threadNum = threadNum1;
		globals = recGlobals;
	}
	public synchronized void modQueue(int num, byte[] msg){
		this.queue.add(num);
		this.msgQueue.add(msg);
	}
	
	public synchronized int pollQueue(){
		int sendOption = this.queue.pollFirst();
		return sendOption;
	}
	public synchronized byte[] pollMsgQueue(){
		byte[] msg = this.msgQueue.pollFirst();
		return msg;
	}
	
//	public synchronized byte[] getMsgQueue(){
//		byte[] msg = this.msgQueue.getFirst();
//		return msg;
//	}
	public void run(){
		//byte[] HSmsg = Handshake.prepare(globals.mypeerid);
		OutputStream out=null;
		DataOutputStream dos=null;
		try {
			out = sock.getOutputStream();
			dos = new DataOutputStream(out);
			//dos.writeInt(HSmsg.length);
			//dos.write(HSmsg);
			//System.out.println("send HS msg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(stopSend!=-1){
			if (Thread.interrupted()){
				break;
			}
 		if(!this.queue.isEmpty()){
 		//while(stopSend!=-1){
 			//if (!this.queue.isEmpty()){
 				int sendOption = this.pollQueue();
 				byte[] msg = this.pollMsgQueue();
 				//System.out.println("ST: sending "+ActualMessage.check(msg) + " message");
 				switch (sendOption){
 					case 0: //choke
 				
 					case 1: //unchoke
// 						byte[] unchokeMsg = ActualMessage.prepareWOpayLoad("unchoke");
// 						try {
// 							dos.write(unchokeMsg);
// 						} catch (IOException e) {
// 							e.printStackTrace();
// 						}
 					case 2: //interested
// 						byte[] interestedMsg = ActualMessage.prepareWOpayLoad("interested");
// 						try {
// 							dos.write(interestedMsg);
// 						} catch (IOException e) {
// 							e.printStackTrace();
// 						}
 					case 3: //not interested
// 						byte[] notInterestedMsg = ActualMessage.prepareWOpayLoad("not interested");
// 						try {
// 							dos.write(notInterestedMsg);
// 						} catch (IOException e) {
// 							e.printStackTrace();
// 						}
 					case 5:	//piece
 					
 				
 					default:	//have, request
// 						byte[] defaultMsg = msgQueue.pollFirst();
// 						try {
// 							dos.write(defaultMsg);
// 						
// 						
// 						} catch (IOException e) {
// 							e.printStackTrace();
// 						}	
 				}
 				try {
 					//byte[] sendMsg=this.pollMsgQueue();
 					//System.out.println("before error:"+ sendMsg.length);
 					dos.flush();
 					int length = msg.length;
 					dos.writeInt(length);
 					dos.write(msg);
					//System.out.println("ST: finished writing" +ActualMessage.check(msg) + " message");
					//this.pollMsgQueue();
				} catch (IOException e) {
					e.printStackTrace();
				}
 		}
		
 		//System.out.println("ST: thread completd");
// 		try {
//			out.close();
//			dos.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
 		
	}
	}}

