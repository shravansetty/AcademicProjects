import java.util.ArrayList;

public class selectReqPiece {
	public static int find(int[] self, int[] other){
		ArrayList<Integer> posIndexes = new ArrayList<Integer>();
		for (int i=0;i<self.length;i++){
			if (self[i]==0&&other[i]==1){
				posIndexes.add(i);
			}
		}
		//remove
		if (posIndexes.size()==0) return -1;
		int rand= (int)(Math.random() * (posIndexes.size()));
		return posIndexes.get(rand)+1;
	}
	public static void main(String[] args){
		byte[] a = new byte[2];
		byte[] b = new byte[2];
		a[0]=12;//00001100
		a[1]=68;//00100100
		//a=0000110000100100
		b[0]=4;//00000100
		b[1]=73;//00101001
		//b=0000010000101001
		byte[] c = new byte[2];
		c[0] = (byte) (a[0]|b[0]);
		c[1] = (byte) (a[1]|b[1]);
		System.out.println((int)c[0]);
		System.out.println((int)c[1]);
		c[0] = (byte) (a[0]^c[0]);
		c[1] = (byte) (a[1]^c[1]);
		System.out.println((int)c[0]);
		System.out.println((int)c[1]);
		System.out.println("break");
		//selectReqPiece.find(a, b);
		//0000010000101001
	}
}
