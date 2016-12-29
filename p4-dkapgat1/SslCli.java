import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SslCli 
{
	public static void main(String args[]) throws Exception 
	{
    		System.setProperty("javax.net.ssl.trustStore", "key");

		//ssl socket
    		SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
    		Socket soc = ssf.createSocket(args[0], Integer.parseInt(args[1]));
		
		//string operation
    		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    		System.out.println("ENTER STRING TO BE SENT TO SERVER:");
		String toServ = br.readLine();
		PrintStream out = new PrintStream(soc.getOutputStream());
      		out.println(toServ);	//string sent to server
	
		out.close();
		br.close();
		soc.close();
  	}
}
