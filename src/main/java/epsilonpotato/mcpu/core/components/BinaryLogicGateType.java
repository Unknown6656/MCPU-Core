package epsilonpotato.mcpu.core.components;

import epsilonpotato.mcpu.util.BiFunction;

/**
 * Represents an enumeration of all known binary logic gate types
 * @author Unknown6656
 */
public enum BinaryLogicGateType
{
    /**
     * Represents an 'and'-gate
     */
    AND((x, y) -> x & y),
    /**
     * Represents a 'nand'-gate
     */
    NAND((x, y) -> ~(x & y)),
    /**
     * Represents an 'or'-gate
     */
    OR((x, y) -> x | y),
    /**
     * Represents a 'nor'-gate
     */
    NOR((x, y) -> ~(x | y)),
    /**
     * Represents a 'xor'-gate
     */
    XOR((x, y) -> x ^ y),
    /**
     * Represents a 'nxor'-gate
     */
    NXOR((x, y) -> ~(x ^ y)),
    /**
     * Represents an implication gate
     */
    IMPL((x, y) -> ~x | y),
    /**
     * Represents an inverse implication gate
     */
    NIMPL((x, y) -> x & ~y);

    private final BiFunction<Integer, Integer, Integer> func;
    
    
    private BinaryLogicGateType(BiFunction<Integer, Integer, Integer> func)
    {
        this.func = func;
    }
    
    /**
     * Evaluates the gate with the two given parameters
     * @param x First parameter
     * @param y Second parameter
     * @return Evaluation result
     */
    public int eval(int x, int y)
    {
        return func.eval(x, y);
    }   
}
