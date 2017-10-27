package epsilonpotato.mcpu.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;


public class Parallel
{
    static final int nCPU = Runtime.getRuntime().availableProcessors();
    
    
    public static <T> void ForEach(Iterable<T> parameters, final Consumer<T> loopBody)
    {
        ExecutorService executor = Executors.newFixedThreadPool(nCPU);
        List<Future<?>> futures = new LinkedList<Future<?>>();
        
        for (final T param : parameters)
        {
            Future<?> future = executor.submit(new Runnable()
            {
                public void run()
                {
                    loopBody.accept(param);
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
    
    public static void For(int start, int stop, final Consumer<Integer> loopBody)
    {
        ExecutorService executor = Executors.newFixedThreadPool(nCPU);
        List<Future<?>> futures = new LinkedList<Future<?>>();
        
        if (start - stop < 4 * nCPU)
            for (int i = start; i < stop; i++)
            {
                final int k = i;
                
                Future<?> future = executor.submit(() -> loopBody.accept(k));
                
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
                        loopBody.accept(j + k * block + start);
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