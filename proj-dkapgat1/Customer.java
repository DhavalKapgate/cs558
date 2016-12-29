import java.io.BufferedReader; 
import java.io.PrintStream; 
import java.io.InputStreamReader;
import java.security.Signature; 
import java.net.Socket; 
import java.util.Formatter; 
import java.security.MessageDigest;  
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import javax.crypto.Cipher;
public class Customer 
{
  	public static void main(String args[]) throws Exception 
	{
    		Socket s = new Socket(args[0], Integer.parseInt(args[1]));
    		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    		PrintStream out = new PrintStream(s.getOutputStream());
    		BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); // taking username and password from client
			
			//////////////////////////////////////////  S1  //////////////////////////////////////////////////////////////////////////
    		
			System.out.print("ID : ");
    		String username=br.readLine();												
			System.out.print("Password : ");
    		String password=br.readLine(); // sending username and password to server
			String hash = (Hashing(password));
    		out.println(username);														//  call 1 cust-psys
			//System.out.println("call 1 cust-psys sent : "+username);
    		out.println(hash); // fetching the result by server							// call 2 cust-psys
    		//System.out.println("call 2 cust-psys sent : hash of password");
			String result = in.readLine();												// ACK 3 psys-cust
			//System.out.println("ACK 3 cust-psys got : "+result);
			while(result.equals("error"))
			{
				System.out.println("the password is incorrect");
				System.out.print("Password : ");
				password=br.readLine();
				hash = (Hashing(password));
				out.println(hash);
				result = in.readLine();													// ACK 3 psys-cust
				//System.out.println("ACK 3 cust-psys got : "+result);
			}
			///////////////////////////////////////////////////////  S3   /////////////////////////////////////////////////////////////
			String list="";
			//take count 
			list=in.readLine();															// ACK 4 psys-cust
			int count=Integer.parseInt(list);
			//System.out.println("ACK 4 psys-cust got count : "+count);
			String List[]=new String[count];
			//take list
			System.out.println("\nLIST OF ITEMS :\n ");
			for(int i=0;i<count;i++)
			{
				List[i] = in.readLine();												// ACK 5 psys-cust
				System.out.println(List[i]);
			}
			//System.out.println("ACK 5 psys-cust got list  ");
			System.out.println("Please enter the item #:");
			int item_no=Integer.parseInt(br.readLine());
			while(item_no>count || item_no==0)
			{
				System.out.println("Invalid item re-enter item# : ");
				item_no=Integer.parseInt(br.readLine());
			}
			System.out.println("Please enter the quantity:");
			String list_split[]=List[item_no-1].split(", ");
			int quantity=Integer.parseInt(br.readLine());
			int avail=Integer.parseInt(list_split[3]);
			if(avail==0)
			{
				System.out.println("Sorry we are out of "+list_split[1]);
				out.println("exit");													//call 5.1 for terminating/exiting customer 
				System.exit(0);
			}
			else if(avail<quantity)
			{
				System.out.println("Sorry the available quantity:"+avail+"\nPlease re-enter quantity : ");
				quantity=Integer.parseInt(br.readLine());
				while(avail<quantity)
				{
					System.out.println("Please reenter quantity less than or equal to "+avail+": ");
					quantity=Integer.parseInt(br.readLine());
				}
			}
			out.println("continue");												//call 5.1 for continue
			////////////////////////////////////////////////////// S4 /////////////////////////////////////////////////////////////////////
			String choice=item_no+" "+quantity;
			ObjectOutputStream outbyte = new ObjectOutputStream(s.getOutputStream());
			
			byte[] toPsys = encrypt("Pup", choice);
			//DS of above message
			byte[] DS=DSsign("Pr"+username.charAt(0),toPsys);
			outbyte.writeObject(DS);													// call 6 cust-psys (send DS)
			//System.out.println("call 6 cust-psys sent DS");
			//send E(Pup, <item# || quantity>) (<item# || quantity>==choice)
			outbyte.writeObject(toPsys);												// call 7 cust-psys 
			//System.out.println("call 7 cust-psys sent E(Pup, <item# || quantity>)");
			// send E(Pub, <name || credit card number>)
			System.out.println("Enter credit card no: ");
			int credit_no=Integer.parseInt(br.readLine());
			String credit=username+" "+credit_no;
			byte[] toBank = encrypt("Pub", credit);
			outbyte.writeObject(toBank);												// call 8 cust-psys
			//System.out.println("call 8 cust-psys sent E(Pub, <name || credit card number>)");
			///////////////////////////////////////////////////// S5///////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////// S6///////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////// S7///////////////////////////////////////////////////////////////////
			
			String status1=in.readLine();									// ACK 11 psys-cust
			status1=status1.substring(4);
			//System.out.println("ACK 11 psys-cust got : "+status1+" "+status1.length());
			
			if(status1.equals("OK"))
			{
				String status2=in.readLine();								// ACK 14 psys-cust
				//System.out.println("ACK 12 psys-cust got : "+status2);
			
				if(status2.equals("OK"))
					System.out.println("we will process your order soon");
				//System.out.println("ACK 14 psys-cust got : "+status2);
			}
			else
			{
				System.out.println("Wrong credit card number");
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			outbyte.close();
			in.close();
    		out.close();
    		s.close();
  	}
	public static String Hashing(String text)throws Exception
   {
	  MessageDigest md = MessageDigest.getInstance("MD5");
	  Formatter form = new Formatter();
	  md.update(text.getBytes());
	  byte[] hash = md.digest();
	  for (byte bytes : hash)
		  form.format("%02x", bytes);
	  String hashed=form.toString();
      return hashed;
   }
   public static byte[] encrypt(String Key, String to_encryp) 
	{
	    byte[] encrypted = null;
	    try 
	    {
	    	ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(Key));
	    	final PublicKey PubKey = (PublicKey) OIS.readObject();
	    	OIS.close();
			final Cipher cipher = Cipher.getInstance("RSA");
	    	cipher.init(Cipher.ENCRYPT_MODE, PubKey);
	    	encrypted = cipher.doFinal(to_encryp.getBytes());
	    } 
	    catch (Exception e) 
	    {
	    }
	    return encrypted;
	}

 	public static String decrypt(String Key, byte[] to_decrypt)
	{
		byte[] decrypted = null;
		try 
		{
		    ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(Key));
		    final PrivateKey Privkey = (PrivateKey) OIS.readObject();
			OIS.close();
		    final Cipher cipher = Cipher.getInstance("RSA");
	      	cipher.init(Cipher.DECRYPT_MODE, Privkey);
		    decrypted = cipher.doFinal(to_decrypt);
		}	 
		catch (Exception e) 
		{
		}
		return new String(decrypted);
	}
	public static byte[] DSsign(String Key, byte[] toPsys)throws Exception
	{
		ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(Key));
		final PrivateKey prkey = (PrivateKey) OIS.readObject();	
		OIS.close();
		Signature sign = Signature.getInstance("MD5WithRSA");
		sign.initSign(prkey);
		byte[] signatureBytes = sign.sign();
		return signatureBytes;
	}
}
