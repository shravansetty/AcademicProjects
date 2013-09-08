import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Bitfield {
	public static boolean check(int peerid,byte[] bitfield){
		int num=(int)peerProcess.numberOfPieces;
		int byteNum;
		int bitNum;
		for (int i=0;i<num;i++){
			byteNum = i/8;
			bitNum = i%8;
			int ANDnum=1;
			ANDnum = ANDnum<<(8-bitNum-1);
			if ((bitfield[byteNum]&ANDnum)>0){
				String str = "peer_";
				str= str + Integer.toString(peerProcess.mypeerid);
				String file = Integer.toString(i)+ ".dat";
				Path path = FileSystems.getDefault().getPath(str, file);
				if (!Files.exists(path)){
					return true;
				}
			}
		}	
		return false;
	}
	//creates bitfield for given peerId
	public static int[] create(int peerId){
		int num=(int)peerProcess.numberOfPieces;
		int[] myBitField = new int[num];
		for (int i=1;i<num;i++){
			String str = "peer_";
			str= str + Integer.toString(peerProcess.mypeerid);
			String file = Integer.toString(i)+ ".dat";
			Path path = FileSystems.getDefault().getPath(str, file);
			if (!Files.exists(path)){
				myBitField[i-1]=1;		
			}else{
				myBitField[i]=0;
			}
		}
		return myBitField;
	}
}
