import Server.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.io.*;
import java.util.*;


class HelloImpl extends HelloPOA {
    private ORB orb;
    
    public void setORB(ORB orb_val) {
        orb = orb_val; 
    }
    
    //In bytes/characters
    //1 for the newline, remove if not using newlines
    public static int sizeOfRecord = 64 + 1;
    
    public static double timeout = .5;    
    
    public static String thisIP = "dfsJapan";
    public static String[] ipAddresses = new String[]{"dfsIreland"};
    public static HashMap<String, ServerClient> locations = new HashMap<String, ServerClient>();
    public static String initialPort = "1090";
    public static String port = "1089";
    
   // public static HashSet<String> localFiles = new HashSet<String>();
    
    public static String pingCommandpt1 = "ping ";
    public static String pingCommandpt2 = " -c 2";
    
    public static boolean connected = false;
    
    
    public HashSet<String> locationsWithFile = new HashSet<String>();    
    public File activeFile;
    
    public RandomAccessFile r;
  
    public boolean open(String filename)
    {
        if(!Server.localFiles.contains(filename))
        {
            if(makeRequestBroadcast(filename))
            {
                activeFile = new File("RecordsDir/" + filename);
                try { r = new RandomAccessFile(activeFile, "r"); }
		catch(Exception e) {System.out.println("RandomAccessFile could not be created -- Found remotely");}
                return true;
            }
            else return false;
        }
        else 
        {
            activeFile = new File("RecordsDir/" + filename);
            try { r = new RandomAccessFile(activeFile, "r"); }
	    catch(Exception e) {System.out.println("RandomAccessFile could not be created -- Found locally");}
            return true;
        }
    }
    
    //Not sure if this is what she wants for the broadcast for requesting files
    public boolean makeRequestBroadcast(String filename)
    {
        locationsWithFile = new HashSet<String>();
        for(String s : locations.keySet())
        {
            locations.get(s).doRequest(filename, thisIP);
        }
        try {
            Thread.sleep((int)(1000*timeout));
        }
        catch (InterruptedException e)
        {
            //Crap;
        }
        
        
        double minPing = Double.MAX_VALUE;
        String minPingIP = "";
        for(String s : locations.keySet())
        {
            double ping = ping(s);
            if(ping < minPing)
            {
                minPing = ping;
                minPingIP = s;
            }
        }
        return writeFile(filename, locations.get(minPingIP).doTransfer(filename));
    }
    
    public boolean writeFile(String filename, String text)
    {
        try {
            File file = new File("RecordsDir/" + filename);
            FileOutputStream output = new FileOutputStream(file);
            output.write(text.getBytes());
            output.close();
            

            
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void request(String filename, String requesteeIP)
    {
	System.out.println(requesteeIP);
        if(Server.localFiles.contains(filename))
        {
            locations.get(requesteeIP).sendResponse(1, thisIP);
        }
        else
        {
            locations.get(requesteeIP).sendResponse(0, thisIP);
        }
    }
    
    public boolean modifyRecord(int index, String newRecord)
    {
			String after = "";
			String before = "";
			try
			{
					
					String changedString = readRecord(index);
					r.close();
					Scanner sc = new Scanner(activeFile);
					String line = "";
					while(sc.hasNext())
					{
						line = sc.nextLine();
						if(line.equals(changedString))
						{	
							break;
						}
						System.out.println("Found a line before");
						before += line;
					}
					System.out.println("Found wanted String");
					while(sc.hasNext())
					{
		       	line = sc.nextLine();
		       	if(line.equals(changedString))
		       	{
           		break;
		       	}
			System.out.println("Found a line after");
		       	after += line;
					}
          newRecord += '\n';
          
					r = new RandomAccessFile(activeFile, "rw");
					r.setLength(0);
          r.seek(0);
    			r.writeChars(before);
    			r.writeChars(newRecord);
    			r.writeChars(after);
          return true;
      }
      catch(IOException e)
      {
	  e.printStackTrace();
          return false;
      }
    }
    
    public String readRecord(int index)
    {
        try{
            byte[] buff = new byte[sizeOfRecord-1];
            r.seek(index*(sizeOfRecord-1));
            r.read(buff, 0, sizeOfRecord-1);
            return new String(buff);
        }
        catch(IOException e)
        {
	     e.printStackTrace();
            return "";
        }
    }

    public boolean changeActiveFile(String newFileName)
    {
        return open(newFileName);
    }
    
    public String transfer(String filename)
    {
        try {
            File f = new File(filename);
            FileInputStream fis = new FileInputStream(filename);
            int i = fis.read();
            String out = "";
            while(i != -1)
            {
                out += (char)i;
                i = fis.read();
            }
            return out;
        }
        catch (FileNotFoundException e)
        {
            return "";
            //Crap;
        }
        catch (IOException e)
        {
            return "";
            //Crap;
        }
    }
    
    public void getResponse(int statusCode, String responseIP)
    {

        if(statusCode == 1)
            locationsWithFile.add(responseIP);
    }

    /**
     * The relavent information in a ping looks like this:
     *
     * rtt min/avg/max/mdev = 18.635/18.677/18.720/0.143 ms
     *
     * This method splits on the '=' then on the '/'s
     * The second element of the second split is then the average ping for that server
    **/
    public double ping(String server)
    {
        return locations.get(server).doPing();
    }
    
    /**
     * Connects to all other known servers and tells them to start connecting too
     * Only done if the current server is not connected, 
     * The client starts this whole process
    **/
    public void startConnectWeb()
    {
        if(!connected)
        {
            connected = true;
            for(int i = 0; i < ipAddresses.length; i++)
            {
                System.out.println("Trying to connect to " + ipAddresses[i]);
//                 try {
//                     ORB orb = ORB.init(new String[]{"-ORBInitialPort", initialPort, "-port", port, "-ORBInitialHost", ipAddresses[i]}, null);
//                     org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
//                     NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
//                     String name = "Hello";
//                     locations.put(ipAddresses[i], HelloHelper.narrow(ncRef.resolve_str(name)));
//                     
//                     locations.get(ipAddresses[i]).startConnectWeb();
//                     
//                     System.out.println("Connection to " + ipAddresses[i] + " is Okay!");
//                 }
//                 catch (Exception e)
//                 {
//                     System.out.println("Issue connecting to server: " + ipAddresses[i]);
//                     e.printStackTrace();
//                 }
                String[] args = new String[]{"-ORBInitialPort", initialPort, "-port", port, "-ORBInitialHost", ipAddresses[i]};
                locations.put(ipAddresses[i], new ServerClient(args, ipAddresses[i]));
		System.out.println(locations.get(ipAddresses[i].toString()));
                //locations.get(ipAddresses[i]).startConnectWeb();
            }
        }
    }
    
}


public class Server {


    public static void main(String args[]) {
        try{
        // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa and activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            HelloImpl helloImpl = new HelloImpl();
            helloImpl.setORB(orb); 

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(helloImpl);
            Hello href = HelloHelper.narrow(ref);
                
            // get the root naming context
            org.omg.CORBA.Object objRef =
                orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            String name = "Hello";
            NameComponent path[] = ncRef.to_name( name );
            ncRef.rebind(path, href);

            System.out.println("Server ready and waiting ...");
            
	    findFiles();

            // wait for invocations from clients
            orb.run();
        } 
            
        catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
            
        System.out.println("HelloServer Exiting ...");  
            
    }
  

    public static HashSet<String> localFiles = new HashSet<String>();
    
    public static void findFiles()
    {
        File f = new File("RecordsDir");
        File[] fs = f.listFiles();
        
        for(File q : fs)
        {
            localFiles.add(q.getName());
        }
    }

}
 
