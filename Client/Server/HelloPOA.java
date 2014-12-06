package Server;


/**
* Server/HelloPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Server.idl
* Saturday, December 6, 2014 4:22:58 AM GMT
*/

public abstract class HelloPOA extends org.omg.PortableServer.Servant
 implements Server.HelloOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("open", new java.lang.Integer (0));
    _methods.put ("request", new java.lang.Integer (1));
    _methods.put ("transfer", new java.lang.Integer (2));
    _methods.put ("modifyRecord", new java.lang.Integer (3));
    _methods.put ("readRecord", new java.lang.Integer (4));
    _methods.put ("changeActiveFile", new java.lang.Integer (5));
    _methods.put ("getResponse", new java.lang.Integer (6));
    _methods.put ("startConnectWeb", new java.lang.Integer (7));
    _methods.put ("ping", new java.lang.Integer (8));
    _methods.put ("getIP", new java.lang.Integer (9));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // Server/Hello/open
       {
         String filename = in.read_string ();
         boolean $result = false;
         $result = this.open (filename);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 1:  // Server/Hello/request
       {
         String filename = in.read_string ();
         String requesteeIP = in.read_string ();
         this.request (filename, requesteeIP);
         out = $rh.createReply();
         break;
       }

       case 2:  // Server/Hello/transfer
       {
         String filename = in.read_string ();
         String $result = null;
         $result = this.transfer (filename);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // Server/Hello/modifyRecord
       {
         int record = in.read_long ();
         String newRecord = in.read_string ();
         boolean $result = false;
         $result = this.modifyRecord (record, newRecord);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 4:  // Server/Hello/readRecord
       {
         int recordNum = in.read_long ();
         String $result = null;
         $result = this.readRecord (recordNum);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // Server/Hello/changeActiveFile
       {
         String newFileName = in.read_string ();
         boolean $result = false;
         $result = this.changeActiveFile (newFileName);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 6:  // Server/Hello/getResponse
       {
         int statusCode = in.read_long ();
         String responseIP = in.read_string ();
         this.getResponse (statusCode, responseIP);
         out = $rh.createReply();
         break;
       }

       case 7:  // Server/Hello/startConnectWeb
       {
         this.startConnectWeb ();
         out = $rh.createReply();
         break;
       }

       case 8:  // Server/Hello/ping
       {
         this.ping ();
         out = $rh.createReply();
         break;
       }

       case 9:  // Server/Hello/getIP
       {
         String $result = null;
         $result = this.getIP ();
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:Server/Hello:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public Hello _this() 
  {
    return HelloHelper.narrow(
    super._this_object());
  }

  public Hello _this(org.omg.CORBA.ORB orb) 
  {
    return HelloHelper.narrow(
    super._this_object(orb));
  }


} // class HelloPOA
