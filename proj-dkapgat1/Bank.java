import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.io.PrintStream; 
import java.net.Socket;
import javax.crypto.Cipher;
import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.io.FileInputStream;
public class Bank
{
	public static void main(String args[])throws IOException
	{
		try
		{
			ServerSocket Bank_s = new ServerSocket(Integer.parseInt(args[0]));
			while(true)
			{
				Socket s=Bank_s.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintStream out = new PrintStream(s.getOutputStream());
				ObjectInputStream bankin = new ObjectInputStream(s.getInputStream());
				//////////////////////////////////////////////////// S6 ////////////////////////////////////////////////////
				byte[] from_Psys=(byte[]) bankin.readObject();	//ACK 9 psys-bank
				//System.out.println("ACK 9 psys-bank got E(Pub, <name || credit card number>)");
				String decrypt=decryptPrivate("Prb",from_Psys);
				//details==(name creditcard)
				String details[] = decrypt.split(" ");
				String filename="balance.txt";
				String tempfile="temp_balance.txt";
				BufferedReader br = new BufferedReader(new FileReader(filename));
				BufferedWriter bw = new BufferedWriter(new FileWriter(tempfile));
				String line="";
				boolean result=false;
			 
				while ((line = br.readLine()) != null)
				{
					//line is item line 
					String part[] = line.split(", ");
					int balance=0;
					//if name is same
					if(part[0].equals(details[0]))
					{  
						//if credit card match
						if(part[1].equals(details[1]))
						{					  
							out.println("OK");									//call 10 bank-psys
							//System.out.println("call 10 bank-psys sent : OK");
							balance=Integer.parseInt(part[2]);
							byte[] bal_deduction=(byte[]) bankin.readObject();	// ACK 12 psys-bank
							//System.out.println("ACK 12 psys-bank got deduction");
							String deduct=decryptPublic("Pup",bal_deduction);
							int deduction=Integer.parseInt(deduct);
							//if(balance<deduction)
							//{
							//	out.println("Not_enough_balance");				// call 13 bank-psys				
							//	System.out.println("call 13 bank-psys sent : Not_enough_balance");
							//}
							//else
							//{
								balance=balance+deduction;
								out.println("success");							// call 13 bank-psys
								//System.out.println("call 13 bank-psys sent : success");
							//}
							br.close();
							br = new BufferedReader(new FileReader(filename));
							String Line;
							String update=part[0]+", "+part[1]+", "+balance;
							while ((Line = br.readLine()) != null) 
							{
								if (Line.contains(line))
									Line = Line.replace(line,update);
								bw.write(Line+"\n");
							}
							
							//System.out.println(line);
							//System.out.println(update);
							br.close();
							// delete oldfile
							File old = new File(filename);
							old.delete();

							// rename
							File temp = new File(tempfile);
							temp.renameTo(old);
							break;
						}
						else
						{
							out.println("error");								//call 10 bank-psys
							//System.out.println("call 10 bank-psys sent : error");
							br.close();
							break;
						}
					}
				}
				
				bankin.close();
				bw.close();
				out.close();
				s.close();
			}
		}
		catch(Exception e)
		{
			System.err.println("Error in Bank server!!!!");
		}
	}
	public static String decryptPrivate(String key_file, byte[] from_Psys)
	{
		byte[] decrypt = null;
		try 
		{
		    ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(key_file));
		    final PrivateKey Privkey = (PrivateKey) OIS.readObject();	  
		    OIS.close();
			final Cipher cipher = Cipher.getInstance("RSA");
	      	cipher.init(Cipher.DECRYPT_MODE, Privkey);
		    decrypt = cipher.doFinal(from_Psys);
		}	 
		catch (Exception e) 
		{
		}
		return new String(decrypt);
	}
	public static String decryptPublic(String key_file, byte[] bal_deduction)
	{
		byte[] decrypt = null;
		try 
		{
		    ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(key_file));
		    final PublicKey Pubkey = (PublicKey) OIS.readObject();	  
		    OIS.close();
			final Cipher cipher = Cipher.getInstance("RSA");
	      	cipher.init(Cipher.DECRYPT_MODE, Pubkey);
		    decrypt = cipher.doFinal(bal_deduction);
		}	 
		catch (Exception e) 
		{
		}
		return new String(decrypt);
	}
}