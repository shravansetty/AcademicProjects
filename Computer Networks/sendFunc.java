import java.net.Socket;


public class sendFunc {
	public static void doit(Globals globals,int type,byte[] msg,int threadNum,Socket s,int toPeer){
		Thread t=null;
		if (globals.getSendThreads(threadNum)!=null)
			t= globals.getSendThreads(threadNum);
		if (t==null){
			//System.out.println("sendFunc: creating a new thread");
			SendNml newOne = new SendNml(s, toPeer,threadNum,globals);
			newOne.modQueue(type, msg);
			globals.modSendClassObjs(newOne,threadNum);
			Thread newThrd = new Thread(newOne);
			globals.modSendThreads(newThrd,threadNum);
			newThrd.start();
			return;
		}
		if (!t.isAlive()){
			//System.out.println("sendFunc: creating a new thread");
			SendNml old = globals.getSendClassObjs(threadNum);
			
			SendNml newOne = new SendNml(s, toPeer,threadNum,globals);
			byte[] temp=old.pollMsgQueue();
			while(temp!=null){
				//Log.write(globals.mypeerid,"one new feature");
				newOne.modQueue(0, temp);
				temp=old.pollMsgQueue();
			}
			
			newOne.modQueue(type, msg);
			globals.modSendClassObjs(newOne,threadNum);
			Thread newThrd = new Thread(newOne);
			globals.modSendThreads(newThrd,threadNum);
			newThrd.start();
		}else{
			//System.out.println("sendFunc: adding to existing thread");
			SendNml temp = globals.getSendClassObjs(threadNum);
			temp.modQueue(type, msg);
		}
		
	}
}
