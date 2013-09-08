import java.util.ArrayList;


public class OptUnchNeighbor implements Runnable{
	Globals globals;
	public OptUnchNeighbor(Globals glb){
		globals=glb;
	}
	public void run(){
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true){
			for (int i=0;i<globals.optUnchNeighbor.length;i++){
				if (globals.getOptUnchNeighbor(i)==1&&globals.getPrefNeighbor(i)==0){
					globals.modOptUnchNeighbor(i);
					globals.getSendThreads(i).interrupt();
					sendFunc.doit(globals, 0, ActualMessage.prepareWOpayLoad("choke"), i, globals.socks.get(i),globals.getToPeers(i));
				}
			}
			ArrayList<Integer> selectOpt = new ArrayList<Integer>();
			for(int i=0;i<globals.toPeers.length;i++){
				int interestStatus = globals.getInterestStatus(i);
				if (interestStatus==1&&globals.getPrefNeighbor(i)==0){
					selectOpt.add(i);
				}
			}
			if (OptUnchNeighbor.chkDwnlComp(globals)) break;
			
					
			if (selectOpt.size()!=0){
				int rand= (int)(Math.random() * (selectOpt.size()));
				int index = selectOpt.get(rand);
				globals.modOptUnchNeighbor(index);
				globals.modHaveChoked(index, 0);
				sendFunc.doit(globals, 1, ActualMessage.prepareWOpayLoad("unchoke"), index, globals.socks.get(index),globals.getToPeers(index));
				Log.prepOptUnchokedLog(globals.mypeerid, globals.getToPeers(index));
			}
			try {
				Thread.sleep(globals.OptimisticUnchokingInterval*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static boolean chkDwnlComp(Globals globals){
		int[] myBF = globals.getMyBitField();
		for (int i=0;i<myBF.length;i++){
			if (myBF[i]==0 || myBF[i]==-1){
				return false;
			}
		}
		for (int i=0;i<globals.toPeers.length;i++){
			int[] otherBF = globals.getBitFields(i);
			for (int k=0;k<otherBF.length;k++){
				if (otherBF[k]==0 || otherBF[k]==-1){
					return false;
				}
			}
		}	
		return true;
	}

}
