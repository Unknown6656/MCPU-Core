
package epsilonpotato.mcpu.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public final class Serializer
{
    private Serializer()
    {
    }
    
    public static byte[] serialize(Serializable obj) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        try
        {
            ObjectOutput out = new ObjectOutputStream(bos);
            
            out.writeObject(obj);
            out.flush();
            
            return bos.toByteArray();
        }
        finally
        {
            try
            {
                bos.close();
            }
            catch (IOException ex)
            {
                return null;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserialize(byte[] data) throws ClassNotFoundException, IOException
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = null;
        
        try
        {
            in = new ObjectInputStream(bis);
            
            return (T)in.readObject();
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (IOException ex)
            {
                return null;
            }
        }
    }

    public static void getBinaryReader(byte[] data, ErrorConsumer<BinaryReader, IOException> callback) throws IOException
    {
        callback.accept(new BinaryReader(data));
    }
    
    public static byte[] fromBinaryWriter(ErrorConsumer<BinaryWriter, IOException> callback) throws IOException
    {
        BinaryWriter wr = new BinaryWriter();
        
        callback.accept(wr);
        
        return wr.toByteArray();
    }
}
