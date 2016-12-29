import java.io.*;
import java.net.*;
class FtpCli
{
    	public static void main(String argv[]) throws Exception 
	{
		Socket sock = new Socket(argv[0], Integer.parseInt(argv[1]));
		PrintWriter out = new PrintWriter(sock.getOutputStream(),true);
		BufferedReader in =new BufferedReader(new InputStreamReader(sock.getInputStream()));
		BufferedReader br =new BufferedReader(new InputStreamReader(System.in));

		while(true)
		{
			System.out.print("ftp > ");
			String command=br.readLine();
			if(command.equals("lls"))
			{
				System.out.println("lls");
				File current=new File(System.getProperty("user.dir"));
				String content[]=current.list();
				for(int i=0;i<content.length;i++)
					System.out.print(content[i]+" ");
				System.out.println();
			}
			else if(command.equals("quit"))
			{
                                out.println("quit");
				sock.close();
				in.close();
				out.close();
				break;
                        }
			else if(command.startsWith("get"))
			{
				String arg[]=command.split(" ");
	                        String fileName=arg[1];

                                out.println(command);
				String frmSer="";
				String fromServer="";
				while ((frmSer=in.readLine())!=null)
                        	{
					fromServer+=frmSer.concat("\n");
					//System.out.println(frmSer);
					int charr=(int)frmSer.charAt(frmSer.length()-1);
//					System.out.println(charr);
					if(charr==26)break;				
				}
				fromServer=fromServer.substring(0,fromServer.length()-2);	//remove eof
				//System.out.print("Cipher from server "+fromServer+"\n");
				String key="security";
				//System.out.println("YAYYYYY");				
				String filename_split=fileName.substring(0,fileName.length()-4);
                        	PrintWriter into_ce = new PrintWriter(filename_split+"_ce.txt", "UTF-8");
        	                into_ce.println(fromServer);
	                        into_ce.close();
//				System.out.println("AGAIN YAYYYY");
//				System.out.println(fromServer.charAt(fromServer.length()-1));
				String Decrypt=decrypt(fromServer,key);
				//System.out.print("server "+Decrypt+"\n");

                                PrintWriter into_cd = new PrintWriter(filename_split+"_cd.txt", "UTF-8");
                                into_cd.println(Decrypt);
                                into_cd.close();
                        }
			else
			{
				out.println(command);
				String fromServer=in.readLine();
				System.out.print(fromServer+"\n");
			}
		}
		sock.close();
  }
	public static String decrypt(String cipher,String key)
        {
                char[][] table=new char[28][28];
		char series[]={'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',' ','\n'};
		for(int i=0;i<28;i++)
			for(int j=0;j<28;j++)
				table[i][j]=series[(j+i)%28];

		String list="";
		String decrypt="";
		for(int i=0;i<28;i++)
			list=list+series[i];

   		for(int i=0;i<cipher.length();i++)
   		{
 			int diff=list.indexOf(key.charAt(i))-list.indexOf(cipher.charAt(i));
			if(diff<0)diff=-diff;
			else if(diff>0)
				diff=28-diff;
		       	decrypt=decrypt+(list.charAt(diff));
         		key=key+(list.charAt(diff));
		}
		return decrypt;
        }
}
