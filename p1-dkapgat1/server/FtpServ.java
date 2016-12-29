import java.io.*;
import java.net.*;
class FtpServ
{
	public static void main(String args[]) throws Exception
	{
	String command;
	String dir="";
       	ServerSocket listen = new ServerSocket(Integer.parseInt(args[0]));
	Socket conn = listen.accept();
	BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
	PrintWriter out = new PrintWriter(conn.getOutputStream(),true);
	while(true)
	{
	  	command = in.readLine();
		if(command.equals("ls"))
		{
			StringBuffer toClient = new StringBuffer();
			File current = new File(System.getProperty("user.dir"));
             		String content[] = current.list();
            		for(int i=0;i<content.length;i++)
            			toClient.append(content[i]+" ");
			out.println(toClient.toString());
		}
		else if(command.startsWith("mkdir"))
		{
				Process proc;
				proc = Runtime.getRuntime().exec(command);
				proc.waitFor();
				out.println();
		}
		else if(command.startsWith("cd"))
		{
				String command_args[] = command.split(" ");
				String current_path=System.getProperty("user.dir");
				System.setProperty("user.dir", current_path+"/"+command_args[1]);
				//directory = directory+str[1]+"/";
				out.println();
		}
		else if(command.equals("pwd"))
		{
			out.println(System.getProperty("user.dir"));
		}
		else if(command.equals("quit"))
	  	{
			conn.close();
			break;
		}
		else if(command.startsWith("get"))
		{
			
			String plain="";
			String arg[]=command.split(" ");
			String fileName=arg[1];
		      	//Create object of FileReader
      			FileReader file = new FileReader(fileName);

      			//Instantiate the BufferedReader Class
      			BufferedReader fromFile = new BufferedReader(file);

      			//Variable to hold the one line data (ASCII)
      			int line;

      			// Read file line by line and print on the console
      			while ((line = fromFile.read()) != -1)
      			{
  				plain+=(char)line;
      			}
  //      		System.out.println("Plain: "+plain);
			String key="security";
			String cipher=encrypt(plain,key);

			String filename_split=fileName.substring(0,fileName.length()-4);
			PrintWriter into_se = new PrintWriter(filename_split+"_se.txt", "UTF-8");
//			System.out.println("CIPHER:"+cipher);
			into_se.println(cipher);
			into_se.close();

			fromFile.close();
			
			out.println(cipher+'\u001a');//append eof
 		}
		else
		{
				Process proc;
                                proc = Runtime.getRuntime().exec(command);
                                proc.waitFor();
                                out.println();
		}
      }}
	public static String encrypt(String plain,String key)
	{
		char[][] table=new char[28][28];
		char series[]={'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',' ','\n'};
		for(int i=0;i<28;i++)
			for(int j=0;j<28;j++)
				table[i][j]=series[(j+i)%28];
		key=key+plain;

		String cipher="";
		String list="";
		String decrypt="";
		for(int i=0;i<28;i++)
			list=list+series[i];
		for(int i=0;i<plain.length();i++)
			cipher=cipher+(table[list.indexOf(plain.charAt(i))][list.indexOf(key.charAt(i))]);
		return cipher;
	}
}
