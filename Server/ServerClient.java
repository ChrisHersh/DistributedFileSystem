 
import Server.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.*;

public class ServerClient
{
    private Hello helloImpl;
    String targetIP;

    public ServerClient(String args[], String targetIP)
    {
        this.targetIP = targetIP;
        try{
            ORB orb = ORB.init(args, null);

            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService"); 
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
    
            String name = "Hello";
            helloImpl = HelloHelper.narrow(ncRef.resolve_str(name));

            String realTargetIP = helloImpl.getIP();
            
            if(realTargetIP.equals(targetIP))
                System.out.println("Connection to " + targetIP + " seemingly OK!");
            else
                System.out.println("Attempted to connect to " + targetIP + " really got " + realTargetIP);
        
            helloImpl.startConnectWeb();
        
        } catch (Exception e) {
            System.out.println("Issue connecting to : " + targetIP);
            System.out.println("ERROR : " + e);
            e.printStackTrace();
          }
    }
    
    public String doTransfer(String filename)
    {
        return helloImpl.transfer(filename);
    }
    
    public void doRequest(String filename, String thisIP)
    {
        helloImpl.request(filename, thisIP);
    }
    
    public void sendResponse(int statusCode, String responseIP)
    {
        helloImpl.getResponse(statusCode, responseIP);
    }

    public void startConnectWeb()
    {
        helloImpl.startConnectWeb();
    }
    
    public void doPing()
    {
        helloImpl.ping();
    }
    
    @Override
    public String toString()
    {
        return targetIP;
    }
    
} 
