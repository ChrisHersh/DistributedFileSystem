package Server;

/**
* Server/HelloHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Server.idl
* Thursday, December 11, 2014 3:02:17 AM GMT
*/

public final class HelloHolder implements org.omg.CORBA.portable.Streamable
{
  public Server.Hello value = null;

  public HelloHolder ()
  {
  }

  public HelloHolder (Server.Hello initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = Server.HelloHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    Server.HelloHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return Server.HelloHelper.type ();
  }

}
