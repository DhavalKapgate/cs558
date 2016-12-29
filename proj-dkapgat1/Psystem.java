import java.io.BufferedReader;
import java.io.BufferedWriter; 
import java.io.PrintStream;
import java.security.Signature;
import java.io.InputStreamReader; 
import java.net.ServerSocket; 
import java.net.Socket; 
import java.io.FileReader;
import java.io.FileWriter; 
import java.security.PrivateKey;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import javax.crypto.Cipher;
import java.io.File;

public class Psystem {
   public static void main(String args[]) throws Exception
   {
      ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
      while(true)
      {
         Socket s = ss.accept();
		
         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         PrintStream out = new PrintStream(s.getOutputStream(),true);
		 String line="";
	     BufferedReader br = new BufferedReader(new FileReader("password.txt")); 
	     
		//////////////////////////////////////// S2  //////////////////////////////////////////////////////////////
		
         String ID=in.readLine();							// ACK 1
		 //System.out.println("ACK 1 psys-cust got: "+ID);
         boolean result=false;
		String start_Letter="";
		 while(result==false)
		 {
			 String hash=in.readLine();						//ACK 2
			//System.out.println("ACK 2 psys-cust got hash of password ");
			while ((line = br.readLine()) != null)
			{
				String part[] = line.split(" ");
				if(part[0].equals(ID))
				{  
					if(part[1].equals(hash))
					{					  
						result=true;
						start_Letter=start_Letter+line.charAt(0);
						break;
					}
				}
			}
			
			if(result)
			{
				out.println("correct");						//call 3 psys-cust
				//System.out.println("call 3 psys-cust sent : "+result);
			}
			else
			{
				out.println("error");						//call 3 psys-cust
				//System.out.println("call 3 psys-cust sent : "+result);
				br.close();
				br = new BufferedReader(new FileReader("password.txt")); 
			}
		 }
		/////////////////////////////////////////////  S3        //////////////////////////////////////////////////////////
		//count no of items
		br.close();
		int count=0;
		br = new BufferedReader(new FileReader("item.txt"));
		while ((line = br.readLine()) != null)
			count++;
		out.println(count);									//call 4 psys-cust
		//System.out.println("call 4 psys-cust sent count: "+count);
		//send the list
		br.close();
		br = new BufferedReader(new FileReader("item.txt"));
		for(int i=0;i<count;i++)
		{	
			out.println(br.readLine());						// call 5 psys-cust
		}
		//System.out.println("call 5 cust-psys sent a list");
		br.close();
		/////////////////////////////////////////////////////// S4 /////////////////////////////////////////////////////////////////////
		String contn=in.readLine();							//ACK 5.1
		//System.out.println(contn);
		if(contn.equals("continue"))
		{	
			ObjectOutputStream outbyte = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream inbyte = new ObjectInputStream(s.getInputStream());
			
			//get DS and verify
			byte[] DS=(byte[]) inbyte.readObject();				// ACK 6 cust-psys (catch DS)
			//System.out.println("ACK 6 cust-psys sent DS");
			boolean verify=DSverify("Pu"+start_Letter,DS);
			///////////////////////////////////////////////////// S5///////////////////////////////////////////////////////////////////
			if(verify)
			{
				//get fcust==E(Pup, <item# || quantity>) (<item# || quantity>==choice)
				byte[] fCust=(byte[]) inbyte.readObject();												// ACK 7 cust-psys
				//System.out.println("ACK 7 cust-psys sent E(Pup, <item# || quantity>)");
				String choice=decrypt("Prp",fCust);
			
				//get toBank==E(Pub, <name || credit card number>) (name || credit card number==credit)
				byte[] toBank=(byte[]) inbyte.readObject();												// ACK 8 cust-psys
				//System.out.println("ACK 8 cust-psys sent E(Pub, <name || credit card number>)");
				//connect to bank server
				///////////////////////////////////////// S6 //////////////////////////////////////////////////////////////////
				Socket bank = new Socket(args[1], Integer.parseInt(args[2]));
				ObjectOutputStream bankout = new ObjectOutputStream(bank.getOutputStream());
				BufferedReader bankin = new BufferedReader(new InputStreamReader(bank.getInputStream()));
				
				bankout.writeObject(toBank);					// call 9 psys-bank
				//System.out.println("call 9 psys-bank sent E(Pub, <name || credit card number>)");
				String status1=bankin.readLine();				// ACK 10 bank-psys
				//System.out.println("ACK 10 bank-psys sent :"+status1);
			
				if(status1.equals("OK"))
				{
					out.println(status1);							//call 11 psys-cust
					//System.out.println("call 11 psys-cust sent :"+status1);
					
					String choices[]=choice.split(" ");
					int itemno=Integer.parseInt(choices[0]);
					int quan=Integer.parseInt(choices[1]);
					br = new BufferedReader(new FileReader("item.txt"));
					for(int i=0;i<itemno;i++)
						line=br.readLine();
					//System.out.println(line);
					String item_details[]=line.split(", ");
					int cost=Integer.parseInt(item_details[2].substring(1));
					br.close();
					cost=cost*quan;
					//System.out.println(cost);
					String Cost=Integer.toString(cost);
					byte[] bal_deduction=encrypt("Prp",Cost);
					bankout.writeObject(bal_deduction);				//call 12 psys-bank
					//System.out.println("call 12 psys-cust sent :"+cost);
					String status2=bankin.readLine();				//ACK 13 bank-psys
					//System.out.println("ACK 13 bank-psys got :"+status2);
				
					out.println("OK");							//call 14 psys-cust
					//System.out.println("call 14 psys-cust sent : OK");
					br = new BufferedReader(new FileReader("item.txt"));
					BufferedWriter bw = new BufferedWriter(new FileWriter("tempitem.txt"));
					quan=Integer.parseInt(item_details[3])-quan;
					String update=item_details[0]+", "+item_details[1]+", "+item_details[2]+", "+quan;
					//System.out.println(line);
					//System.out.println(update);
					String Line;
					while ((Line = br.readLine()) != null) 
					{
						if (Line.contains(line))
							Line = Line.replace(line,update);
						bw.write(Line+"\n");
					}
					// delete oldfile
					File old = new File("item.txt");
					old.delete();

					// rename
					File temp = new File("tempitem.txt");
					temp.renameTo(old);
					
					br.close();
					bw.close();
				}
				else if(status1.equals("error"))
				{
					out.println("error");							//call 11 psys-cust
					//System.out.println("call 11 psys-cust sent :"+status1);
				}
				bankout.close();
				bankin.close();
				bank.close();
			}
			out.close();
			outbyte.close();
			inbyte.close();
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         in.close();
         s.close();
      }
   }
public static byte[] encrypt(String Key, String Cost) 
	{
	    byte[] encrypted = null;
	    try 
	    {
	    	ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(Key));
	    	final PrivateKey key = (PrivateKey) OIS.readObject();
	    	OIS.close();
			
			final Cipher cipher = Cipher.getInstance("RSA");
	    	cipher.init(Cipher.ENCRYPT_MODE, key);
	    	encrypted = cipher.doFinal(Cost.getBytes());
	    } 
	    catch (Exception e) 
	    {
	    }
	    return encrypted;
	}

 	public static String decrypt(String Key, byte[] fCust)
	{
		byte[] decrypted = null;
		try 
		{
		    ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(Key));
		    final PrivateKey key = (PrivateKey) OIS.readObject();	  
		    OIS.close();
			
			final Cipher cipher = Cipher.getInstance("RSA");
	      	    cipher.init(Cipher.DECRYPT_MODE, key);
		    decrypted = cipher.doFinal(fCust);
		}	 
		catch (Exception ex) 
		{
		}
		return new String(decrypted);
	}
	public static boolean DSverify(String key, byte[] DS)throws Exception
	{
		ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(key));
		final PublicKey publickey = (PublicKey) OIS.readObject();
		OIS.close();
		Signature sign = Signature.getInstance("MD5WithRSA");
		sign.initVerify(publickey);
		boolean verify = sign.verify(DS);
		return verify;
	}
}
