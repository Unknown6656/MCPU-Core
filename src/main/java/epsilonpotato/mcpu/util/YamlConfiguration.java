package epsilonpotato.mcpu.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


/**
 * Represents a YAML configuration file
 * @author Zedly
 */
public final class YamlConfiguration implements Map<String, Object>
{   
    private final Map<String, Object> map;
    
    
    private YamlConfiguration(Map<String, Object> content)
    {
        map = content;
    }
    
    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object oPath)
    {
        return get(oPath) != null;
    }
    
    /**
     * Interprets the value stored at the given path as a boolean value and returns it
     * @param path Value path
     * @param defaultValue Default value, if the path could not be found
     * @return Fetched value
     */
    public boolean getBoolean(String path, boolean defaultValue)
    {
        Object value = get(path);

        if (value == null)
            return defaultValue;
        else if (value instanceof Boolean)
            return (boolean)value;
        else
            try
            {
                return Boolean.parseBoolean(value.toString());
            }
            catch (Exception e)
            {
                return defaultValue;
            }
    }

    /**
     * Interprets the value stored at the given path as a double value and returns it
     * @param path Value path
     * @param defaultValue Default value, if the path could not be found
     * @return Fetched value
     */
    public double getDouble(String path, double defaultValue)
    {
        Object value = get(path);

        if (value == null)
            return defaultValue;
        else if (value instanceof Double)
            return (double)value;
        else
            try
            {
                return Double.parseDouble(value.toString());
            }
            catch (Exception e)
            {
                return defaultValue;
            }
    }

    /**
     * Interprets the value stored at the given path as an int32 value and returns it
     * @param path Value path
     * @param defaultValue Default value, if the path could not be found
     * @return Fetched value
     */
    public int getInt(String path, int defaultValue)
    {
        Object value = get(path);

        if (value == null)
            return defaultValue;
        else if (value instanceof Integer)
            return (int)value;
        else
            try
            {
                return Integer.parseInt(value.toString());
            }
            catch (Exception e)
            {
                return defaultValue;
            }
    }

    /**
     * Interprets the value stored at the given path as a value list and returns it
     * @param <T> Generic list type
     * @param path Value path
     * @param t List type class
     * @return Fetched list
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path, Class<T> t)
    {
        Object value = get(path);
        
        if (value instanceof List)
            try
            {
                return (List<T>)value;
            }
            catch (ClassCastException ex)
            {
                return null;
            }
        else
            return null;
    }

    /**
     * Interprets the value stored at the given path as an int64 value and returns it
     * @param path Value path
     * @param defaultValue Default value, if the path could not be found
     * @return Fetched value
     */
    public long getLong(String path, long defaultValue)
    {
        Object value = get(path);
        
        if (value == null)
            return defaultValue;
        else if (value instanceof Long)
            return (long)value;
        else
            try
            {
                return Long.parseLong(value.toString());
            }
            catch (Exception e)
            {
                return defaultValue;
            }
    }
    
    /**
     * Fetches all sections stored at the given path and returns them as list
     * @param path Path
     * @return Section list
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<YamlConfiguration> getSectionList(String path)
    {
        List<YamlConfiguration> configs = new ArrayList<>();
        Object value = get(path);
        
        if (value instanceof List)
            for (Object o : (List)value)
                try
                {
                    if (o instanceof Map)
                        configs.add(new YamlConfiguration((Map<String, Object>)o));
                }
                catch (ClassCastException ex)
                {
                }
        
        return configs;
    }

    /**
     * Returns the section stored at the given path or creates a new one if none could be found
     * @param path Path
     * @return Section
     */
    @SuppressWarnings("unchecked")
    public YamlConfiguration getOrCreateSection(String path)
    {
        Object value = get(path);
        
        if (value instanceof Map)
            return new YamlConfiguration((Map<String, Object>)value);
        else
        {
            Map<String, Object> newMap = new LinkedHashMap<>();
            
            set(path, newMap);
            
            return new YamlConfiguration(newMap);
        }
    }

    /**
     * Interprets the value stored at the given path as a string value and returns it
     * @param path Value path
     * @param defaultValue Default value, if the path could not be found
     * @return Fetched value
     */
    public String getString(String path, String defaultValue)
    {
        Object value = get(path);
        
        return value instanceof String ? (String)value : value != null ? value.toString() : defaultValue;
    }

    /**
     * Interprets the value stored at the given path as an UUID value and returns it
     * @param path Value path
     * @param defaultValue Default value, if the path could not be found
     * @return Fetched value
     */
    public UUID getUUID(String path, UUID defaultValue)
    {   
        Object uuid = get(path);

        if (uuid == null)
            return defaultValue;
        else if (uuid instanceof UUID)
            return (UUID)uuid;
        else
            try
            {
                System.out.println(uuid.toString());
                
                return UUID.fromString(uuid instanceof String ? (String)uuid : uuid.toString());
            }
            catch (Exception e)
            {
                return defaultValue;
            }
    }
    
    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object get(Object oPath)
    {
        if (!(oPath instanceof String))
            return null;
        
        String path = (String)oPath;
        String[] sections = path.split("(?<!\\\\)\\.");
        
        for (int i = 0; i < sections.length; i++)
            sections[i] = sections[i].replace("\\.", ".");
        
        Map<String, Object> innerMap = map;
        
        for (int i = 0; i < sections.length - 1; i++)
        {
            Object o = innerMap.get(sections[i]);
            
            if (o == null)
                return null;
            else if (o instanceof Map)
                innerMap = (Map<String, Object>)o;
            else
                return null;
        }
        
        Object value = innerMap.get(sections[sections.length - 1]);

        return value instanceof Map ? new YamlConfiguration((Map)value) : innerMap.get(sections[sections.length - 1]);
    }
    
    /**
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }
    
    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet()
    {
        return map.keySet();
    }
    
    /**
     * Saves the current YAML configuration into the given file
     * @param file Target file
     * @throws IOException Thrown if an I/O error occurs
     */
    public void save(File file) throws IOException
    {
        DumperOptions options = new DumperOptions();
        
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        
        save(file, options);
    }
    
    /**
     * Saves the current YAML configuration into the given file
     * @param file Target file
     * @param options Output options
     * @throws IOException Thrown if an I/O error occurs
     */
    public void save(File file, DumperOptions options) throws IOException
    {
        new Yaml(options).dump(map, new FileWriter(file, false));
    }
    
    /**
     * Saves the current YAML configuration into the given output stream
     * @param stream Output stream
     * @throws IOException Thrown if an I/O error occurs
     */
    public void save(OutputStream stream) throws IOException
    {
        DumperOptions options = new DumperOptions();
        
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        
        save(stream, options);
    }
    
    /**
     * Saves the current YAML configuration into the given output stream
     * @param stream Output stream
     * @param options Output options
     * @throws IOException Thrown if an I/O error occurs
     */
    public void save(OutputStream stream, DumperOptions options) throws IOException
    {
        new Yaml(options).dump(map, new OutputStreamWriter(stream, "UTF-8"));
    }
    
    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(String key, Object value)
    {
        set(key, value);
        
        return value;
    }
    
    /**
     * Sets the value at the given path to the new given one
     * @param path Value path
     * @param value New value
     * @return Indicates whether the operation was successful
     */
    @SuppressWarnings("unchecked")
    public boolean set(String path, Object value)
    {
        String[] sections = path.split("\\.");
        Map<String, Object> innerMap = map;
        
        for (int i = 0; i < sections.length - 1; i++)
        {
            Object o = innerMap.get(sections[i]);
            
            if (o == null)
            {
                Map<String, Object> newMap = new LinkedHashMap<>();
                
                innerMap.put(sections[i], newMap);
                innerMap = newMap;
            }
            else if (o instanceof Map)
                innerMap = (Map<String, Object>)o;
            else
            {
                System.out.println(o);
                
                return false;
            }
        }

        innerMap.put(sections[sections.length - 1], value instanceof YamlConfiguration ? ((YamlConfiguration)value).map : value);
        
        return true;
    }
    
    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size()
    {
        return map.size();
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return map.toString();
    }
    
    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }
    
    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key)
    {
        return map.remove(key);
    }
    
    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m)
    {
        map.putAll(m);
    }
    
    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear()
    {
        map.clear();
    }
    
    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<Object> values()
    {
        return map.values();
    }
    
    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        return map.entrySet();
    }

    /**
     * Represents an empty YAML configuration
     * @return Empty YAML configuration
     */
    public static YamlConfiguration emptyConfiguration()
    {
        return new YamlConfiguration(new LinkedHashMap<>());
    }
    
    /**
     * Reads a YAML configuration from the given input file and returns it
     * @param file Input file
     * @return YAML configuration
     */
    public static YamlConfiguration read(File file)
    {
        try
        {
            return read(new FileReader(file));
        }
        catch (IOException ex)
        {
            return emptyConfiguration();
        }
    }
    
    /**
     * Reads a YAML configuration from the given input stream and returns it
     * @param is Input stream
     * @return YAML configuration
     */
    public static YamlConfiguration read(InputStream is)
    {
        return read(new InputStreamReader(is));
    }
    
    /**
     * Reads a YAML configuration from the given reader instance and returns it
     * @param rdr Stream reader
     * @return YAML configuration
     */
    @SuppressWarnings("unchecked")
    public static YamlConfiguration read(Reader rdr)
    {
        try
        {
            Object o = new Yaml().load(rdr);

            return o instanceof Map ? new YamlConfiguration((Map<String, Object>)o) : emptyConfiguration();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            
            return emptyConfiguration();
        }
    }
}
