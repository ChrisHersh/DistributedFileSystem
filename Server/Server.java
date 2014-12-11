import Server.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.io.*;
import java.util.*;
import java.nio.file.*;


class HelloImpl extends HelloPOA {
    private ORB orb;
    
    public void setORB(ORB orb_val) {
        orb = orb_val; 
    }
    
    //In bytes/characters
    //1 for the newline, remove if not using newlines
    public static int sizeOfRecord = 10 + 1;
    
    public static double timeout = .5;    
    
    public static HashMap<String, ServerClient> locations = new HashMap<String, ServerClient>();
    public static String initialPort = "1090";
    public static String port = "1089";
    public static String baseDir = "RecordsDir/";
    
    public static boolean connected = false;
    
    public static boolean debug = true;
    
    
    public HashMap<Integer, HashSet<String>> locationsWithFile = new HashMap<Integer, HashSet<String>>();
    public HashMap<Integer, File> activeFile = new HashMap<Integer, File>();
    public HashMap<Integer, RandomAccessFile> r = new HashMap<Integer, RandomAccessFile>();
  
    public boolean open(String filename, int key)
    {
        if(!Server.localFiles.contains(filename))
        {
            if(makeRequestBroadcast(filename, key))
            {
                activeFile.put(key, new File(baseDir + filename));
                try { r.put(key, new RandomAccessFile(activeFile.get(key), "r")); }
		catch(Exception e) {System.out.println("RandomAccessFile could not be created -- Found remotely");}
                return true;
            }
            else return false;
        }
        else 
        {
            activeFile.put(key, new File(baseDir + filename));
            try { r.put(key, new RandomAccessFile(activeFile.get(key), "r")); }
	    catch(Exception e) {System.out.println("RandomAccessFile could not be created -- Found locally");}
            return true;
        }
    }
    
    //Not sure if this is what she wants for the broadcast for requesting files
    public boolean makeRequestBroadcast(String filename, int key)
    {
        locationsWithFile.put(key, new HashSet<String>());
        for(String s : locations.keySet())
        {
            locations.get(s).doRequest(filename, Server.thisIP, key);
        }
        
        double minPing = Double.MAX_VALUE;
        String minPingIP = "";
        for(String s : locationsWithFile.get(key))
        {
            double start = System.nanoTime();
            locations.get(s).doPing();
            double end = System.nanoTime();
            
            double ping = end - start;
            
            System.out.println(s + " ping was " + (ping/1000.0/1000.0) + "ms");
            
            if(ping < minPing)
            {
                minPing = ping;
                minPingIP = s;
            }
        }
        
        System.out.println("Chose " + minPingIP + " as best server, requesting transfer"); 
        
        return writeFile(filename, locations.get(minPingIP).doTransfer(filename));
    }
    
    public boolean writeFile(String filename, String text)
    {
        try {
            File file = new File(baseDir + filename);
            FileOutputStream output = new FileOutputStream(file);
            output.write(text.getBytes());
            output.close();
	    Server.localFiles.add(filename);            
            return true;
        } catch ( IOException e ) {
//             e.printStackTrace();
	    System.out.println("___Something Happened___:\n\t" + e);
            return false;
        }
    }
    
    public void request(String filename, String requesteeIP, int key)
    {
	System.out.println("Recieved query for " + filename + " from " + requesteeIP);
        if(Server.localFiles.contains(filename))
        {
            locations.get(requesteeIP).sendResponse(1, Server.thisIP, key);
        }
        else
        {
            locations.get(requesteeIP).sendResponse(0, Server.thisIP, key);
        }
    }
    
    public boolean modifyRecord(int index, String newRecord, int key)
    {
        String filename = activeFile.get(key).getName();
        String after = "";
        String before = "";
        try
        {  
            String changedString = readRecord(index, key);
            r.get(key).close();
            Scanner sc = new Scanner(activeFile.get(key));
            String line = "";
            while(sc.hasNext())
            {
                line = sc.nextLine();
                if(line.equals(changedString))
                    break;
                before += line + '\n';
            }
                        
            while(sc.hasNext())
            {
                line = sc.nextLine();
                if(line.equals(changedString))
                    break;
                after += line + '\n';
            }
            newRecord += '\n';

            try {
                Files.delete(FileSystems.getDefault().getPath("RecordsDir", filename));     
            } catch (Exception e) {
                //             e.printStackTrace();
		System.out.println("___Something Happened___:\n\t" + e);
            }
            
            writeFile(filename, before + newRecord + after);
            
            r.put(key, new RandomAccessFile(activeFile.get(key), "r"));
            
            sendDeleteSignal(filename, key);
            
            return true;
        }
        catch(IOException e)
        {
            //             e.printStackTrace();
	    System.out.println("___Something Happened___:\n\t" + e);
            return false;
        }
    }
    
    public String readRecord(int index, int key)
    {
        try{
            byte[] buff = new byte[sizeOfRecord-1];
            r.get(key).seek(index*(sizeOfRecord));
            r.get(key).read(buff, 0, sizeOfRecord-1);
            return new String(buff);
        }
        catch(IOException e)
        {
	    //             e.printStackTrace();
	    System.out.println("___Something Happened___:\n\t" + e);
            return "";
        }
    }

    public boolean changeActiveFile(String newFileName, int key)
    {
        return open(newFileName, key);
    }
    
    public String transfer(String filename)
    {
        System.out.println("Recieved transfer request. Beginning transfer");
        try {
            File f = new File(baseDir + filename);
            FileInputStream fis = new FileInputStream(f);
            int i = fis.read();
            String out = "";
            while(i != -1)
            {
                out += (char)i;
                i = fis.read();
            }
            
            System.out.println("Transfer ending successfully");
            
            return out;
        }
        catch (FileNotFoundException e)
        {
            //             e.printStackTrace();
	    System.out.println("___Something Happened___:\n\t" + e);
            return "";
            //Crap;
        }
        catch (IOException e)
        {
            //             e.printStackTrace();
	    System.out.println("___Something Happened___:\n\t" + e);
            return "";
            //Crap;
        }
    }
    
    public void getResponse(int statusCode, String responseIP, int key)
    {
        if(statusCode == 1)
            locationsWithFile.get(key).add(responseIP);
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
            for(int i = 0; i < Server.ipAddresses.length; i++)
            {
                System.out.println("Trying to connect to " + Server.ipAddresses[i]);
                String[] args = new String[]{"-ORBInitialPort", initialPort, "-port", port, "-ORBInitialHost", Server.ipAddresses[i]};
                locations.put(Server.ipAddresses[i], new ServerClient(args, Server.ipAddresses[i]));
            }
        }
    }
    
    public String getIP()
    {
        return Server.thisIP;
    }
    
    public void ping()
    {
        return;
    }
    
    public String getLocalFiles()
    {
        String s = "";
        for(String str : Server.localFiles)
        {
            s += str + "\n";
        }
        return s;
    }
    
    public String getAllKnownFiles()
    {
        String s = "";
        for(String ip : locations.keySet())
        {
            s += "\n" + ip + ":\n";
            s += locations.get(ip).getLocalFiles();
        }
        s += "\nLocal Files for " + Server.thisIP + ":\n";
        s += getLocalFiles();
        
        return s;
    }
    
    public void sendDeleteSignal(String filename, int key)
    {
        locationsWithFile.put(key, new HashSet<String>());
        for(String s : Server.ipAddresses)
        {
            locations.get(s).doRequest(filename, Server.thisIP, key);
        }
    
    
        for(String s : locationsWithFile.get(key))
        {
            locations.get(s).sendDelete(filename, Server.thisIP);
        }
    }
    
    public void deleteFile(String filename, String requestee)
    {
        try {
            Files.delete(FileSystems.getDefault().getPath("RecordsDir", filename));
            writeFile(filename, locations.get(requestee).doTransfer(filename));
            
            for(Integer i : activeFile.keySet())
            {
                if(activeFile.get(i).getName().equals(filename))
                {
                    changeActiveFile(filename, i);
                }
            }
            
        } catch(IOException e) {
            System.out.println(filename + " was not able to be deleted on this server");
        }
    }
    
    public void clientQuit(int key)
    {
        //Remove all traces of that user
        locationsWithFile.remove(key);
        activeFile.remove(key);
        r.remove(key);
        System.out.println("Client with key: " + key + " just quit");
    }
}


public class Server {

    public static String thisIP;
    public static String[] ipAddresses;
    public static HashSet<String> localFiles = new HashSet<String>();

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
            
            readInitFile();
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
    
    public static void findFiles()
    {
        File f = new File("RecordsDir");
        File[] fs = f.listFiles();
        
        System.out.println("Files found locally:");
        
        for(File q : fs)
        {
            System.out.println(q.getName());
            localFiles.add(q.getName());
        }
    }
    
    public static void readInitFile()
    {
        Scanner s = null;
        try {
            s = new Scanner(new File("ips.txt")); 
        } catch (FileNotFoundException e) {
            System.out.println("This server needs an ips.txt file, server will now crash, have a nice day");
            Integer.parseInt("Crash"); //Kills the server, easier than dealing with it otherwise
            //System.exit is for pansies who try to avoid crashing
        }
            
        thisIP = s.nextLine();
        int num = s.nextInt();
        s.nextLine();
        ipAddresses = new String[num];
        for(int i = 0; i < num; i++)
        {
            ipAddresses[i] = s.nextLine();
        }
    }
}
 
