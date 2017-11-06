
package epsilonpotato.mcpu.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * A class containing serialisation helper methods
 * @author Unknown6656
 */
public final class Serializer
{
    private Serializer()
    {
    }
    
    /**
     * Serialises the given object to a byte array 
     * @param obj Object to be serialised
     * @return Byte array
     * @throws IOException Thrown if an I/O error occurs while writing stream header
     */
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
    
    /**
     * Deserialises an object from the given byte array
     * @param <T> Generic output type
     * @param data Byte array
     * @return Deserialised object
     * @throws ClassNotFoundException Thrown if the class of a serialised object cannot be found
     * @throws IOException Thrown if an I/O error occurs while reading stream header
     */
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

    /**
     * Invokes the given callback function with a binary reader created from the given byte array
     * @param data Byte array
     * @param callback Callback function
     * @throws IOException Thrown if any I/O error occurs
     */
    public static void getBinaryReader(byte[] data, ErrorAction<BinaryReader, IOException> callback) throws IOException
    {
        callback.eval(new BinaryReader(data));
    }
    
    /**
     * Invokes the given callback function with a binary writer and returns the created byte array
     * @param callback Callback function
     * @return Created byte array
     * @throws IOException Thrown if any I/O error occurs
     */
    public static byte[] fromBinaryWriter(ErrorAction<BinaryWriter, IOException> callback) throws IOException
    {
        BinaryWriter wr = new BinaryWriter();
        
        callback.eval(wr);
        
        return wr.toByteArray();
    }
}
