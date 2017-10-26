package epsilonpotato.mcpu.util;

// https://github.com/Top-Cat/SteamKit-Java


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.google.protobuf.CodedOutputStream;


public final class BinaryWriter
{
    CodedOutputStream writer;
    OutputStream os;
    ByteArrayOutputStream stream = null;
    
    
    public BinaryWriter(ByteArrayOutputStream stream)
    {
        this((OutputStream)stream);
        this.stream = stream;
    }
    
    public BinaryWriter(int size)
    {
        this(new ByteArrayOutputStream(size));
    }
    
    public BinaryWriter()
    {
        this(32);
    }
    
    public BinaryWriter(OutputStream outputStream)
    {
        os = outputStream;
        writer = CodedOutputStream.newInstance(outputStream);
    }

    public void write(UUID data) throws IOException
    {
        write(data.getMostSignificantBits());
        write(data.getLeastSignificantBits());
    }
    
    public void write(int[] data) throws IOException
    {
        if (data != null)
        {
            write(data.length);
            
            for (int i = 0; i < data.length; ++i)
                write(data[i]);
        }
        else
            write(0);
    }
    
    public void write(short data) throws IOException
    {
        final ByteBuffer buffer = ByteBuffer.allocate(2);
        
        buffer.putShort(data);
        writeR(buffer);
    }
    
    public void write(int data) throws IOException
    {
        final ByteBuffer buffer = ByteBuffer.allocate(4);
        
        buffer.putInt(data);
        writeR(buffer);
    }
    
    public void write(long data) throws IOException
    {
        final ByteBuffer buffer = ByteBuffer.allocate(8);
        
        buffer.putLong(data);
        writeR(buffer);
    }
    
    public void write(String data) throws IOException
    {
        byte[] bytes = data.getBytes();
        
        write(bytes.length);
        write(bytes);
    }
    
    public byte[] toByteArray()
    {
        return stream != null ? stream.toByteArray() : null;
    }
    
    public void writeR(ByteBuffer buffer) throws IOException
    {
        for (int i = buffer.capacity() - 1; i >= 0; --i)
            write(buffer.get(i));
    }
    
    public void write(byte[] data) throws IOException
    {
        writer.writeRawBytes(data);
        writer.flush();
    }
    
    public void write(byte data) throws IOException
    {
        writer.writeRawByte(data);
        writer.flush();
    }
    
    public CodedOutputStream getStream()
    {
        return writer;
    }
    
    public void flush()
    {
        try
        {
            os.flush();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }
}
