import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
public class ActualMessage {
	public static byte[] prepareWOpayLoad(String type){
		switch (type){
			case "choke":
				byte[] length;
				length=ByteBuffer.allocate(4).putInt(1).array();
				byte[] msg = new byte[5];
				for (int i=0;i<4;i++){
					msg[i]=length[i];
				}
				msg[4]=0;
				return msg;
			case "unchoke":
				byte[] length1;
 				length1=ByteBuffer.allocate(4).putInt(1).array();
				msg = new byte[5];
				for (int i=0;i<4;i++){
					msg[i]=length1[i];
				}
				msg[4]=1;
				return msg;
			case "interested":
				byte[] length2;
				length2=ByteBuffer.allocate(4).putInt(1).array();
				msg = new byte[5];
				for (int i=0;i<4;i++){
					msg[i]=length2[i];
				}
				msg[4]=2;
				return msg;
			case "not interested":
				byte[] length3;
				length3=ByteBuffer.allocate(4).putInt(1).array();
				msg = new byte[5];
				for (int i=0;i<4;i++){
					msg[i]=length3[i];
				}
				msg[4]=3;
				return msg;	
			case "bitfield" :
				//System.out.println("AM:preparing bitfield msg");
				int num;
				num=(int)peerProcess.numberOfPieces;
				byte[] pieces;
				byte[] length4;
				if (num%8==0){
					length4=ByteBuffer.allocate(4).putInt(1+num/8).array();	
					pieces = new byte[num/8+5];
				}
				else{
					length4=ByteBuffer.allocate(4).putInt(2+num/8).array();
					pieces = new byte[num/8+6];
				}
				pieces[4]=5;
				int byteNum;
				int bitNum;
				for (int i=1;i<=num;i++){
					byteNum = (i-1)/8;
					bitNum = 7-(i-1)%8;
					String str = "peer_";
					str= str + Integer.toString(peerProcess.mypeerid);
					String file = Integer.toString(i)+ ".dat";
					Path path = FileSystems.getDefault().getPath(str, file);
					if (Files.exists(path)){
						byte temp =pieces[5+byteNum];
				 		temp= (byte) (temp | (1<<bitNum));
				 		pieces[5+byteNum] = temp;
				 	}
				 }
				 for (int i=0;i<4;i++){
					 pieces[i]=length4[i];
				 }
				 return pieces;
		}
		return null;
	}
	public static byte[] prepHave(int index){
		byte[] msg;
		msg= new byte[9];
		byte[] length=ByteBuffer.allocate(4).putInt(5).array();
		for (int i=0;i<4;i++){
			msg[i]=length[i];
		}
		msg[4]=4;
		byte[] payload;
		payload=ByteBuffer.allocate(4).putInt(index).array();
		for (int i=0;i<4;i++){
			msg[i+5]=payload[i];
		}
		return msg;
	}
	public static byte[] prepRequest(int index){
		byte[] msg;
		msg= new byte[9];
		byte[] length;
		length=ByteBuffer.allocate(4).putInt(5).array();
		for (int i=0;i<4;i++){
			msg[i]=length[i];
		}
		msg[4]=6;
		byte[] payload;
		payload=ByteBuffer.allocate(4).putInt(index).array();
		for (int i=0;i<4;i++){
			msg[i+5]=payload[i];
		}
		return msg;
	}
	public static byte[] prepPiece(int piece){
		String str = "peer_";
		str= str + Integer.toString(peerProcess.mypeerid);
		String file = Integer.toString(piece)+ ".dat";
		Path path = FileSystems.getDefault().getPath(str, file);
		byte[] fileBytes=null;
		try {
			fileBytes= Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] msg;
		msg= new byte[9+fileBytes.length];
		msg[4]=7;
		for(int i=0;i<fileBytes.length;i++){
			msg[9+i]=fileBytes[i];
		}
		byte[] length;
		length=ByteBuffer.allocate(4).putInt(9+fileBytes.length).array();
		for (int i=0;i<4;i++){
			msg[i]=length[i];
		}
		byte[] pieceNum;
		pieceNum=ByteBuffer.allocate(4).putInt(piece).array();
		for (int i=0;i<4;i++){
			msg[5+i]=pieceNum[i];
		}
		return msg;
	}
	public static String check(byte[] msg){
		switch (msg[4]){
			case 0:
				return "choke";
			case 1:
				return "unchoke";
			case 2:
				return "interested";
			case 3:
				return "not interested";
			case 4:
				return "have";
			case 5:
				return "bitfield";
			case 6:
				return "request";
			case 7:
				return "piece";
			default:
				return "handshake";
		}
		//return null;
	}
	public static int retHavePayload(byte[] msg){
		byte[] pieceNum = new byte[4];
		for (int i=0;i<4;i++) pieceNum[i]=msg[5+i];
		ByteBuffer wrapped = ByteBuffer.wrap(pieceNum);
		int index=wrapped.getInt();
		return index;
	}
	public static int retRequestPayload(byte[] msg){
		byte[] pieceNum = new byte[4];
		for (int i=0;i<4;i++) pieceNum[i]=msg[5+i];
		ByteBuffer wrapped = ByteBuffer.wrap(pieceNum);
		int index=wrapped.getInt();
		return index;
	}
	public static byte[] retBitfieldPayload(byte[] msg){
		byte[] bitfield = new byte[msg.length-5];
		for(int i=0;i<msg.length-5;i++){
			bitfield[i] = msg[5+i];
		}
		return bitfield;
	}
	public static int parsePicePayload(byte[] msg, int peerNum){
		//System.out.println("AM:parsing piece payload");
		byte[] pieceNumBytes = new byte[4];
		for (int i=0;i<4;i++){
			pieceNumBytes[i]=msg[5+i];
		}
		ByteBuffer wrapped = ByteBuffer.wrap(pieceNumBytes);
		int pieceNum=wrapped.getInt();
		
		FileOutputStream out;
		
//		String str = "peer_";
//		str= str + Integer.toString(peerProcess.mypeerid);
//		String file = Integer.toString(pieceNum)+ ".dat";
//		Path path = FileSystems.getDefault().getPath(str, file);
		byte[] fileBytes;
		fileBytes= new byte[msg.length-9];
		for (int i=0;i<fileBytes.length;i++){
			fileBytes[i]=msg[9+i];
		}
		try {
			//Files.write(path,fileBytes);
			File dir = new File("peer_" + peerNum);
			dir.mkdir();
			File newFile = new File(dir, String.format("%d", pieceNum)
					+ ".dat");
			newFile.createNewFile();
			out = new FileOutputStream(newFile);
			out.write(fileBytes, 0, fileBytes.length);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pieceNum;
	}
	public static void main(String[] args){
		byte[] test=new byte[11];
		test[0]=0;
		test[1]=0;
		test[2]=0;
		test[3]=7;
		test[4]=5;
		test[5]=-1;
		test[6]=-1;
		test[7]=-1;
		test[8]=-1;
		test[9]=-1;
		test[10]=-8;
		//System.out.println(ActualMessage.check(test));
		
	}
}
