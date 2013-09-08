
public class Stop implements Runnable {
	Globals globals;
	public Stop(Globals glb){
		globals=glb;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int time;
		if ((int)globals.FileSize>617565) time=600000;
		else time = 120000;
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (globals.pt.isAlive()) globals.pt.interrupt();
 if (globals.oun.isAlive()) globals.oun.interrupt();
	}
	

}
