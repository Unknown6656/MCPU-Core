package epsilonpotato.mcpu.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * A class containing parallelisation utility functions
 * @author Unknown6656
 */
public final class Parallel
{
    static final int nCPU = Runtime.getRuntime().availableProcessors();
    
    
    private Parallel()
    {
    }
    
    /**
     * Executes the given function for every element in the given collection in parallel
     * @param <T> Generic collection type
     * @param collection Collection
     * @param func Iteration function
     */
    public static <T> void ForEach(Iterable<T> collection, final Action<T> func)
    {
        ExecutorService executor = Executors.newFixedThreadPool(nCPU);
        List<Future<?>> futures = new LinkedList<Future<?>>();
        
        for (final T param : collection)
        {
            Future<?> future = executor.submit(new Runnable()
            {
                public void run()
                {
                    func.eval(param);
                }
            });
            
            futures.add(future);
        }
        
        for (Future<?> f : futures)
            try
            {
                f.get();
            }
            catch (InterruptedException e)
            {
            }
            catch (ExecutionException e)
            {
            }
        
        executor.shutdown();
    }
    
    /**
     * Executes the given function for every integer in the given range in parallel
     * @param start Integer lower inclusive range limit
     * @param stop Integer upper exclusive range limit
     * @param func Iteration function
     */
    public static void For(int start, int stop, final Action<Integer> func)
    {
        ExecutorService executor = Executors.newFixedThreadPool(nCPU);
        List<Future<?>> futures = new LinkedList<Future<?>>();
        
        if (start - stop < 4 * nCPU)
            for (int i = start; i < stop; i++)
            {
                final int k = i;
                
                Future<?> future = executor.submit(() -> func.eval(k));
                
                futures.add(future);
            }
        else
            for (int i = 0; i < nCPU; ++i)
            {
                final int k = i;
                final int block = (int)((stop - start) / (float)nCPU);

                futures.add(executor.submit(() ->
                {
                    for (int j = 0, l = k < nCPU - 1 ? block : stop - start - block * k; j < l; ++j)
                        func.eval(j + k * block + start);
                }));
            }
        
        for (Future<?> f : futures)
            try
            {
                f.get();
            }
            catch (InterruptedException e)
            {
            }
            catch (ExecutionException e)
            {
            }
        
        executor.shutdown();
    }
}