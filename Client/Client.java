 
import Server.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.*;
import java.math.*;

public class Client
{
  static Hello helloImpl;
  static int key;

  public static void main(String args[])
    {
      try{
        ORB orb = ORB.init(args, null);

        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService"); 
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
 
        String name = "Hello";
        helloImpl = HelloHelper.narrow(ncRef.resolve_str(name));
        key = (int)(Math.random()*Integer.MAX_VALUE);
        
        System.out.println("This session's key is: " + key);
        
        helloImpl.startConnectWeb();
	
        System.out.println("Obtained a handle on server object: " + helloImpl);
//         System.out.println(helloImpl.sayHello());
//         helloImpl.shutdown();

        Scanner s = new Scanner(System.in);

        changeFile(s, helloImpl);
        
        String strSelection = givePrompt(s);
        
        boolean notQuitting = true;
        
        while(notQuitting)
        {
            try
            {
                int selection = Integer.parseInt(strSelection);
                //read
                if(selection == 1)
                {
                    read(s, helloImpl);
                }
                //write
                else if(selection == 2)
                {
                    write(s, helloImpl);
                }
                //change file
                else if(selection == 3)
                {
                    changeFile(s, helloImpl);
                }
                //get local files
                else if(selection == 4)
                {
                    getLocal(helloImpl);
                }
                //get all files
                else if(selection == 5)
                {
                    getAll(helloImpl);
                }
            }
            catch (NumberFormatException e)
            {
            
                if(strSelection.equals("q") || strSelection.equals("exit"))
                {
                    notQuitting = false;
                }
                else
                {
                    System.out.println("That was not a valid selection");
                }
            }
            
            if(notQuitting)
                strSelection = givePrompt(s);
        }
        
        helloImpl.clientQuit(key);

        } catch (Exception e) {
          System.out.println("ERROR : " + e) ;
          e.printStackTrace(System.out);
          }
    }
    
    public static String givePrompt(Scanner s)
    {
        System.out.println("What you want? (1 is read, 2 is write, 3 is change opened file, 4 is display all local files, 5 is display all known files):");
        return s.next();
    }
    
    public static void read(Scanner s, Hello helloImpl)
    {
        System.out.print("Please enter the record number you want: ");
        int recordNum = s.nextInt();
	String str = helloImpl.readRecord(recordNum, key);
	if(str == null)
		System.out.println("Nothing found");
	else
        	System.out.println(str);
    }
    
    public static void write(Scanner s, Hello helloImpl)
    {
        System.out.print("Please enter the record number you want to change: ");
        int recordNum = 0;
        recordNum = s.nextInt();
        
        String targetString = helloImpl.readRecord(recordNum, key);
        System.out.println("Is " + targetString + " the record you want to replace? [y/n]");
        String reply = s.next();
        if(!reply.equals("y"))
            return;
        
        System.out.print("Please enter the new record of 10 characters: ");
        s.nextLine();
        String newRecord = s.nextLine();
        
        helloImpl.modifyRecord(recordNum, newRecord, key);
    }
    
    public static void changeFile(Scanner s, Hello helloImpl)
    {
        System.out.print("Please enter the file you want to open (No spaces): ");
        String fileName = s.next();
        
        helloImpl.open(fileName, key);
    }
    
    public static void getLocal(Hello helloImpl)
    {
        System.out.println(helloImpl.getLocalFiles());
    }
    
    public static void getAll(Hello helloImpl)
    {
        System.out.println(helloImpl.getAllKnownFiles());
    }

} 
