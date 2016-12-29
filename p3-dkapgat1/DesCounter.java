import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.KeyGenerator;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.IOException;

class DesEncrypter 
{
  	Cipher ecipher;
  	Cipher dcipher;
  	DesEncrypter(SecretKey key) throws Exception 
	{
  	  	ecipher = Cipher.getInstance("DES");
  	  	dcipher = Cipher.getInstance("DES");
    		ecipher.init(Cipher.ENCRYPT_MODE, key);
    		dcipher.init(Cipher.DECRYPT_MODE, key);
  	}
	
	//from http://www.java2s.com/Code/Java/Security/EncryptingaStringwithDES.html
  	public String encrypt(String str) throws Exception 
	{
	    	// Encode the string into bytes using utf-8
    		byte[] utf8 = str.getBytes("UTF8");

		// Encrypt
    		byte[] enc = ecipher.doFinal(utf8);
	
    		// Encode bytes to base64 to get a string
    		return new sun.misc.BASE64Encoder().encode(enc);
  	}

  	public String decrypt(String str) throws Exception 
	{
		// Decode base64 to get bytes
    		byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);

    		byte[] utf8 = dcipher.doFinal(dec);

    		// Decode using utf-8
    		return new String(utf8, "UTF8");
  	}
}

public class DesCounter 
{
  	//from http://tomcov2.googlecode.com/svn-history/r20/tomcov/src/com/tomcov/server/Cryptage.java
	public static SecretKey Encrypt () throws Exception
	{
		byte[] raw = new byte[]{0x01, 0x72, 0x43, 0x3E, 0x1C, 0x7A, 0x55};
		byte[] keyBytes = addParity(raw);
		//for(int i=0;i<8;i++)System.out.println(keyBytes[i]);
		SecretKey key = new SecretKeySpec(keyBytes, "DES");
		
		return key;
	}
	public static byte[] addParity(byte[] in) 
	{
		byte[] result = new byte[8];
		// Keeps track of the bit position in the result
		int resultIx = 1;
		// Used to keep track of the number of 1 bits in each 7-bit chunk
		int bitCount = 0;
		// Process each of the 56 bits
		for (int i=0; i<56; i++) 
		{
			// Get the bit at bit position i
			boolean bit = (in[6-i/8]&(1<<(i%8))) > 0;

			// If set, set the corresponding bit in the result
			if (bit) 
			{
				result[7-resultIx/8] |= (1<<(resultIx%8))&0xFF;
				bitCount++;
			}

			// Set the parity bit after every 7 bits
			if ((i+1) % 7 == 0) 
			{
				if (bitCount % 2 == 0) 
				{
					// Set low-order bit (parity bit) if bit count is even
					result[7-resultIx/8] |= 1;
				}
				resultIx++;
				bitCount = 0;
			}
			resultIx++;
		}
		return result;
	}
	public static byte[] XOR(String encrypted,String P)
	{
		byte[] encrypt=encrypted.getBytes();
                byte[] message=P.getBytes();
                for(int i=0;i<8;i++)
                	encrypt[i]=(byte)(encrypt[i]^message[i]);
                return encrypt;
	}
  	public static void main(String[] args) throws Exception 
	{
    		SecretKey key=Encrypt(); 
		DesEncrypter encrypter = new DesEncrypter(key);
		//System.out.println("Key: "+key+"\n");
		String counter="00001234";
		int count,no_of_char=0;	
		String encrypted="",decrypted="";
    		File file = new File(args[0]);
    		String sb="";
      		try 
		{
      			FileInputStream fis = new FileInputStream(file);
			File f=new File(args[1]);
			if(!f.exists())
				f.createNewFile();
			PrintWriter pw= new PrintWriter(f);
			int mode=Integer.parseInt(args[2]);
      			char current;
      			while (fis.available() > 0) 
			{
        			current = (char) fis.read(); 
        			sb=sb+current;
				no_of_char++;
      			}

			String P="";
			int no_of_block=0;
			while(sb.length()>0)
			{
				if(sb.length()>=8)
				{
					P=sb.substring(0,8);
					sb=sb.substring(8,sb.length());
					no_of_block++;
				}
				else
				{
					P=sb;
					sb="";
					for(int i=P.length();i<8;i++)
						P=P+" ";
					no_of_block++;
				}
				encrypted = encrypter.encrypt(counter);
		    		//System.out.println(counter+" ENCRYPTED to "+encrypted);
            			
				byte[] encrypt=XOR(encrypted,P);
				String temp=new String(encrypt);
				encrypted=temp;
				//System.out.println(counter+" ENCRYPTED to "+encrypted+" "+encrypted.length());
				if(mode==1)		//encryption so store all characters 
				{
					pw.print(encrypted.substring(0,8));
				}
				else
				{
					if(encrypted.indexOf(" ")!=-1)
						pw.print(encrypted.substring(0,encrypted.indexOf(" ")));
					else
						pw.print(encrypted.substring(0,8));
				}
        			count=Integer.parseInt(counter);
	    			count++;
	    			counter=Integer.toString(count);
	    			if(count<10000)
	    				counter="0000"+counter;
	    			else if(count<100000)
	            			counter="000"+counter;
    				else if(count<1000000)
              				counter="00"+counter;
      				else if(count<10000000)
              				counter="0"+counter;
	//			System.out.println(P);
			}
//			if(mode==1)
				System.out.println("No of characters read : "+no_of_char+" No of blocks : "+no_of_block);
			fis.close();
                        pw.close();
    		}	
		catch (IOException e) 
		{
      			e.printStackTrace();
    		}
  	}
}
