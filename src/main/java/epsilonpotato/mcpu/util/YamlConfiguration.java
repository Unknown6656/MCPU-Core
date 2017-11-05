/* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor. */

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
 * @author Dennis
 */
public final class YamlConfiguration implements Map<String, Object>
{   
    private final Map<String, Object> map;
    
    
    private YamlConfiguration(Map<String, Object> content)
    {
        map = content;
    }
    
    @Override
    public boolean containsKey(Object oPath)
    {
        return get(oPath) != null;
    }
    
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

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path, Class<T> tClass)
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
    
    public String getString(String path, String defaultValue)
    {
        Object value = get(path);
        
        return value instanceof String ? (String)value : value != null ? value.toString() : defaultValue;
    }
    
    public UUID getUUID(String path, UUID defaultValue)
    {   
        Object uuid = get(path);

        if (uuid == null)
            return defaultValue;
        else if (uuid instanceof UUID)
            return (UUID)uuid;
        else if (uuid instanceof String)
            return UUID.fromString((String)uuid);
        else
            return UUID.fromString(uuid.toString());
    }
    
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
    
    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }
    
    @Override
    public Set<String> keySet()
    {
        return map.keySet();
    }
    
    public void save(File file) throws IOException
    {
        DumperOptions options = new DumperOptions();
        
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        
        save(file, options);
    }
    
    public void save(File file, DumperOptions options) throws IOException
    {
        new Yaml(options).dump(map, new FileWriter(file, false));
    }
    
    public void save(OutputStream stream) throws IOException
    {
        DumperOptions options = new DumperOptions();
        
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        
        save(stream, options);
    }
    
    public void save(OutputStream stream, DumperOptions options) throws IOException
    {
        new Yaml(options).dump(map, new OutputStreamWriter(stream, "UTF-8"));
    }
    
    @Override
    public Object put(String key, Object value)
    {
        set(key, value);
        
        return value;
    }
    
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
    
    @Override
    public int size()
    {
        return map.size();
    }
    
    @Override
    public String toString()
    {
        return map.toString();
    }
    
    @Override
    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }
    
    @Override
    public Object remove(Object key)
    {
        return map.remove(key);
    }
    
    @Override
    public void putAll(Map<? extends String, ? extends Object> m)
    {
        map.putAll(m);
    }
    
    @Override
    public void clear()
    {
        map.clear();
    }
    
    @Override
    public Collection<Object> values()
    {
        return map.values();
    }
    
    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        return map.entrySet();
    }

    public static YamlConfiguration emptyConfiguration()
    {
        return new YamlConfiguration(new LinkedHashMap<>());
    }
    
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
    
    public static YamlConfiguration read(InputStream is)
    {
        return read(new InputStreamReader(is));
    }
    
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
