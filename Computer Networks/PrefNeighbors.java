import java.util.ArrayList;
import java.util.Arrays;

public class PrefNeighbors implements Runnable {
	Globals globals;

	public PrefNeighbors(Globals globals1) {
		globals = globals1;
	}

	public void run() {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		////System.out.println("starting pref thread");
		int hasFile = 1;
		int allOthersHaveFiles = 1;
		//int[] BF = globals.getMyBitField();
		while (true) {
			int[] BF = globals.getMyBitField();
			hasFile = 1;
			allOthersHaveFiles = 1;
			int i;
			for (i = 0; i < BF.length; i++) {
				//System.out.print(BF[i]);
				if (BF[i] == 0 ||BF[i] == -1 ) {
					// //System.out.println("hasfile=0");
					//Log.write(globals.mypeerid, "Failure point: " + i + " " + BF[i]);
					hasFile = 0;
					break;
				}
			}
			if (i == BF.length)
				hasFile = 1;
			
			////System.out.println("allothershavefiles:"+allOthersHaveFiles );
			for (i = 0; i < globals.toPeers.length; i++) {
				int[] othersBF = globals.getBitFields(i);
				if (othersBF == null)
					continue;
				String s=i+ " ";
				for(int x=0;x<othersBF.length;x++) s= s + othersBF[x];
				//Log.write(globals.mypeerid, "bitfield of: " +s);
				for (int k = 0; k < othersBF.length; k++) {
					//System.out.print("others bitfield:" + othersBF[k]);
					if (othersBF[k] == 0 || othersBF[k] == -1) {
						//Log.write(globals.mypeerid, "Failure point: " +i+" "+ k+" " +othersBF[k]);
						allOthersHaveFiles = 0;
						break;
					}
				}
				if (allOthersHaveFiles == 0) {
					break;
				}
			}
			////System.out.println("allothershavefiles:"+allOthersHaveFiles );
			if (hasFile == 1 && allOthersHaveFiles == 1) {
				////System.out.println("PTTTTTTTTTTTTT: breaking out of PT");
				for (int j=0;j<globals.toPeers.length;j++){
					Thread t = globals.getSendThreads(j);
					if (t.isAlive()){
						////System.out.println("interruptingggg:" + j);
						t.interrupt();
					}else{
						////System.out.println("not alive for :" + j);
					}
					
					Thread t1 = globals.getRecThreads(j);
					if (t1.isAlive()){
						////System.out.println("interruptingggg:" + j);
						t1.interrupt();
					}else{
						////System.out.println("not alive for :" + j);
					}
				}
				
				if (globals.oun.isAlive()){
					globals.oun.interrupt();
				}
				// globals.peerRecNml.get(0).stop();
				break;
			}
			if (hasFile == 1) {
				////System.out.println("hasfile=1");
				ArrayList<Integer> selectPref = new ArrayList<Integer>();
				for (i = 0; i < globals.toPeers.length; i++) {
					int interestStatus = globals.getInterestStatus(i);
					if (interestStatus == 1) {
						////System.out.println("PT: added interested " + i);
						selectPref.add(i);
					} else {// if not interested but unchoked then choke it
						if (globals.getHaveChoked(i) == 0&&globals.getOptUnchNeighbor(i)!=1) {
							globals.getSendThreads(i).interrupt();
							
							//int reqPiece=globals.getReqPiece(i);
							//globals.modReqPiece(i, 0);
							//if (reqPiece!=0)globals.modBitFields(i, reqPiece-1, 0);
							
							sendFunc.doit(globals, 0,
									ActualMessage.prepareWOpayLoad("choke"),
									i, globals.socks.get(i),
									globals.getToPeers(i));
							globals.modHaveChoked(i, 1);
						}
						globals.modPrefNeighbor(i,0);

					}
					
				}
				while (selectPref.size() > globals.NumberOfPreferredNeighbors) {
					int rand = (int) (Math.random() * (selectPref.size()));
					int index = selectPref.get(rand);
					////System.out.println("index is: " + index);
					////System.out.println("wtf is this :"+ globals.getHaveChoked(index));
					if (globals.getHaveChoked(index) == 0&&globals.getOptUnchNeighbor(index)!=1) {// if
																			// interested
																			// but
																			// unlucky
																			// and
																			// if
																			// unchoked
																			// then
																			// choke
						globals.getSendThreads(index).interrupt();
//						int reqPiece=globals.getReqPiece(index);
//						globals.modReqPiece(index, 0);
//						if (reqPiece!=0)globals.modBitFields(index, reqPiece-1, 0);
						
						sendFunc.doit(globals, 0,
								ActualMessage.prepareWOpayLoad("choke"),
								index, globals.socks.get(index),
								globals.getToPeers(index));
						globals.modHaveChoked(index, 1);
					}
					globals.modPrefNeighbor(index,0);
					selectPref.remove(rand);
					//selectPref.add(rand,-1);
				}
				for (i = 0; i < selectPref.size(); i++) {
					int index = selectPref.get(i);
					if (index==-1) continue;
					globals.modPrefNeighbor(index,1);
					if (globals.getHaveChoked(index) == 1) {
						////System.out.println("first time unchoked");
						globals.modHaveChoked(index, 0);
						sendFunc.doit(globals, 1,
								ActualMessage.prepareWOpayLoad("unchoke"),
								index, globals.socks.get(index),
								globals.getToPeers(index));
					} else {
						//System.out.println("do nothing");
						// globals.modHaveChoked(i, 0);
						// sendFunc.doit(globals, 1,
						// ActualMessage.prepareWOpayLoad("unchoke"), i,
						// globals.socks.get(i),globals.peerId[i]);
					}
				}
			} else {
				int length = globals.getNumOfInterestedPeers(); 
				if ( length != 0) {
					DownSpeed[] speeds = new DownSpeed[length];
					int count = 0;
					for (i = 0; i < globals.toPeers.length; i++) {
						// //System.out.println("PTTTTTTTTTTTTTTTTTTTTTTTTTTTT: "
						// + i + " neighbor");
						if (globals.getInterestStatus(i) == 1) {
							// //System.out.println("PTTTTTTTTTTTTTTTTTTTTTTTTTTTT: "
							// + i + " added to speeds array");
							DownSpeed newSpd = new DownSpeed(
									globals.getDownSpeed(i), i);
							speeds[count] = newSpd;
							count++;
						} else {
							if (globals.getHaveChoked(i) == 0&&globals.getOptUnchNeighbor(i)!=1) {// if not
																// interested
																// and if
																// unchoked then
																// choke
								// //System.out.println("PTTTTTTTTTTTTTTTTTTTTTTTTTTTT: "
								// + i + " being choked");
								globals.getSendThreads(i).interrupt();
//								int reqPiece = globals.getReqPiece(i);
//								globals.modReqPiece(i, 0);
//								if (reqPiece!=0)globals.modBitFields(i, reqPiece-1, 0);
								
								sendFunc.doit(
										globals,
										0,
										ActualMessage.prepareWOpayLoad("choke"),
										i, globals.socks.get(i),
										globals.getToPeers(i));
								globals.modHaveChoked(i, 1);
	
							}
						}
					}
					// if (speeds[0]!=null){
					//System.out.println("oooooooooooo " + speeds.length);
					for (Object o : speeds) {
						//System.out.println(o);
					}
					Arrays.sort(speeds, new DownSpeedComp());
					for (i = 0; i < speeds.length; i++) {
						if (i < globals.NumberOfPreferredNeighbors) {
							// //System.out.println("PTTTTTTTTTTTTTTTTTTTTTTTTTTTT:speed["+
							// i + "] is " +speeds[i]);
							if (speeds[i] == null)
								continue;
							int index = speeds[i].index;
							if (globals.getHaveChoked(index) == 1) {
								globals.modHaveChoked(index, 0);
								sendFunc.doit(globals, 1, ActualMessage
										.prepareWOpayLoad("unchoke"), index,
										globals.socks.get(index),
										globals.getToPeers(index));
							}
							
							
							
							//change thread stop to thread interrupt
							
							
							
							
							
						} else {
							if (speeds[i] == null)
								continue;
							int index = speeds[i].index;
							if (globals.getHaveChoked(index) == 0
									&& globals.getOptUnchNeighbor(index) != 1) {
								globals.modHaveChoked(index, 1);
								globals.getSendThreads(index).interrupt();
//								int reqPiece = globals.getReqPiece(index);
//								globals.modReqPiece(index, 0);
//								if (reqPiece!=0)globals.modBitFields(index, reqPiece-1, 0);
								sendFunc.doit(
										globals,
										0,
										ActualMessage.prepareWOpayLoad("choke"),
										index, globals.socks.get(index),
										globals.getToPeers(index));
							}
						}
					}
					// }
				}else{
					for (int k=0;k<globals.toPeers.length;k++){
						if (globals.getInterestStatus(k)==0&&globals.getHaveChoked(k)==0&&globals.getOptUnchNeighbor(k)!=1){
							globals.getSendThreads(k).interrupt();
//							int reqPiece = globals.getReqPiece(k);
//							globals.modReqPiece(k, 0);
//							if (reqPiece!=0)globals.modBitFields(k, reqPiece-1, 0);
							sendFunc.doit(
									globals,
									0,
									ActualMessage.prepareWOpayLoad("choke"),
									k, globals.socks.get(k),
									globals.getToPeers(k));
							globals.modHaveChoked(k, 1);
						}
					}
				}
				
			}
			try {
				//System.out.println("pref thread into sleep");
				Thread.sleep(globals.UnchokingInterval * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("pref thread out of sleep");
		}
	}

}
