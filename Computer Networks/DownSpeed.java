

public class DownSpeed implements Comparable<DownSpeed>{
	int speed;
	int index;
	public DownSpeed(int spd, int ind){
		speed = spd;
		index = ind;
	}
	public int compareTo(DownSpeed in){
		int inSpeed = in.speed;
		return inSpeed - this.speed;
	}
//	public static Comparator<DownSpeed> FruitNameComparator 
//    = new Comparator<DownSpeed>() {
//
//		public int compare(DownSpeed spd1,DownSpeed spd2) {
//
//			String fruitName1 = fruit1.getFruitName().toUpperCase();
//			String fruitName2 = fruit2.getFruitName().toUpperCase();
//
//			//ascending order
//			return fruitName1.compareTo(fruitName2);
//
//			//descending order
//			//return fruitName2.compareTo(fruitName1);
//		}
//
//	};
}
