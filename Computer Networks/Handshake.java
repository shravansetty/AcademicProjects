
public class Handshake {
	public static byte[] prepare(int peerId){
		String msg="CEN5501C2008SPRING0000000000";
		byte[] msg1 = new byte[32];
		msg1=msg.getBytes();
		byte[] msg2 = new byte[32];
		for (int i = 0; i < 28; i++) {
		    msg2[i] = msg1[i];
		}
		int temp=peerId;
		for (int i = 31; i > 27; i--) {
			msg2[i]=(byte) (temp%10);
			//System.out.println("HS"+ msg2[i]);
			temp=temp/10;
		}
		//System.out.println(msg2[31]);
		//System.out.print(new String(msg2));
		return msg2;
	}
	public static boolean check(int neighborId,byte[] HSmsg){
		byte[] header=new byte[18];
		for(int i=0;i<18;i++){
			header[i]=HSmsg[i];
			//System.out.println(header[i]);
		}
		//System.out.println(new String(header));
		if (!(new String(header)).equals("CEN5501C2008SPRING")){
			return false;
		}
		for(int i = 31; i > 27; i--){
			if (neighborId%10!=(int)HSmsg[i]){
				return false;
			}
			neighborId=neighborId/10;
		}
		return true;
	}
	public static void main(String[] args){
		byte[] msg=Handshake.prepare(1001);
		System.out.println(Handshake.check(1001,msg));
	}
}
