import java.util.Comparator;


public class DownSpeedComp implements Comparator<DownSpeed> {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public int compare(DownSpeed arg0, DownSpeed arg1) {
		System.out.println(arg1.speed+ "-" +arg0.speed);
		return arg1.speed - arg0.speed;
	}

}
