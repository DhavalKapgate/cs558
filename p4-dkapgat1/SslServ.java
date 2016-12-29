import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class SslServ 
{
  	public static void main(String args[]) throws Exception 
	{
		//certificate operations
    		System.setProperty("javax.net.ssl.keyStore", "key");
    		System.setProperty("javax.net.ssl.keyStorePassword", "Security");
		
		//ssl socket establishment 
    		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    		ServerSocket ss = ssf.createServerSocket(Integer.parseInt(args[0]));

      		Socket soc = ss.accept();
		System.out.println("WAITING FOR CLIENT TO SEND STRING...");			
		//string operations
      		BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
    		String fromCli = br.readLine();
    		System.out.println("STRING RECEIVED FROM CLIENT:\n"+fromCli+"\nof length : "+fromCli.length());
    			
		br.close();
      		soc.close();
  	}
}
