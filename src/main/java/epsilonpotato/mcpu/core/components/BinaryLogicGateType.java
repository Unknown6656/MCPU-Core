package epsilonpotato.mcpu.core.components;

import epsilonpotato.mcpu.util.BiFunction;

public enum BinaryLogicGateType
{
    AND((x, y) -> x & y),
    NAND((x, y) -> ~(x & y)),
    OR((x, y) -> x | y),
    NOR((x, y) -> ~(x | y)),
    XOR((x, y) -> x ^ y),
    NXOR((x, y) -> ~(x ^ y)),
    IMPL((x, y) -> ~x | y),
    NIMPL((x, y) -> x & ~y);

    private final BiFunction<Integer, Integer, Integer> func;
    
    
    private BinaryLogicGateType(BiFunction<Integer, Integer, Integer> func)
    {
        this.func = func;
    }
    
    public int eval(int x, int y)
    {
        return func.eval(x, y);
    }   
}
