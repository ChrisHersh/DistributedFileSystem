 
import Server.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.*;

public class Client
{
  static Hello helloImpl;

  public static void main(String args[])
    {
      try{
        ORB orb = ORB.init(args, null);

        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService"); 
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
 
        String name = "Hello";
        helloImpl = HelloHelper.narrow(ncRef.resolve_str(name));
        
        helloImpl.startConnectWeb();

        System.out.println("Obtained a handle on server object: " + helloImpl);
//         System.out.println(helloImpl.sayHello());
//         helloImpl.shutdown();

        Scanner s = new Scanner(System.in);

        System.out.print("Please enter the file you want to open (No spaces): ");
        String fileName = s.next();
        
        helloImpl.open(fileName);
        
        System.out.println("What you want?");
        int selection = s.nextInt();
        
        while(true)
        {
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
            
            else
            {
            //Fuck
            }
            
            
            System.out.println("What you want?");
            selection = s.nextInt();
        }
        
//         System.out.print("Please enter the record number you want (type exit to quit): ");
//         int recordNum = 0;
//         boolean exit = false;
//         try
//         {
//             recordNum = s.nextInt();
//         }
//         catch (InputMismatchException e)
//         {
//             exit = true;
//         }
//         
//         while(!exit)
//         {
//             System.out.println(helloImpl.readRecord(recordNum));
//             
//             System.out.print("Please enter the record number you want (type exit to quit): ");
//             exit = false;
//             try
//             {
//                 recordNum = s.nextInt();
//             }
//             catch (InputMismatchException e)
//             {
//                 exit = true;
//             }
//         }

        } catch (Exception e) {
          System.out.println("ERROR : " + e) ;
          e.printStackTrace(System.out);
          }
    }
    
    public static void read(Scanner s, Hello helloImpl)
    {
        System.out.print("Please enter the record number you want: ");
        int recordNum = s.nextInt();
	String str = helloImpl.readRecord(recordNum);
	if(str == null)
		System.out.println("Nothing found");
	else
        	System.out.println(helloImpl.readRecord(recordNum));
    }
    
    public static void write(Scanner s, Hello helloImpl)
    {
        System.out.print("Please enter the record number you want to change: ");
        int recordNum = 0;
        recordNum = s.nextInt();
        
        System.out.print("Please enter the new record of 64 characters: ");
        s.nextLine();
        String newRecord = s.nextLine();
        
        helloImpl.modifyRecord(recordNum, newRecord);
    }

} 
