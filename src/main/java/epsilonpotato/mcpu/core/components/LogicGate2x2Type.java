package epsilonpotato.mcpu.core.components;


import epsilonpotato.mcpu.util.*;

public enum LogicGate2x2Type
{
    HALF_ADDER((g, s) ->
    {
        g[2] = g[0] ^ g[1];
        g[3] = g[0] & g[1];
    }),
    T_FLIP_FLOP((g, s) ->
    {
        if (g[0] != 0)
            s[0] = (byte)(s[0] == 0 ? 1 : 2); // set only on rising flank
        else
            s[0] = 0;
        
        if (s[0] == 1) // if rising flank
            s[1] = (byte)(s[1] != 0 ? 0 : 1);
        
        if (g[1] != 0)
            s[1] = 0;
        
        g[2] = s[1];
        g[3] = g[2] != 0 ? 0 : 1;
    });
    
    private final BiAction<int[], byte[]> func;
    
    
    private LogicGate2x2Type(BiAction<int[], byte[]> func)
    {
        this.func = func;
    }
    
    public void eval(int[] gates, byte[] states)
    {
        func.eval(gates, states);
    }
}
