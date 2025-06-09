import org.dnttr.zephyr.serializer.annotations.Map;
import org.dnttr.zephyr.serializer.annotations.Serializable;

/**
 * @author dnttr
 */

@Serializable
public class SerializableObject {

    @Map(address = "a")
    public byte a = 127;
    
    @Map(address = "b")
    public short b = -128;

    @Map(address = "c")
    public char c = 'c';
    
    @Map(address = "d")
    public int d = -32768;
    
    @Map(address = "e")
    public long e = -9223372036854775807L;
    
    @Map(address = "f")
    public float f = -1.0F;
    
    @Map(address = "g")
    public double g = -1.0D;
    
    @Map(address = "h")
    public String h = "hello";

    @Map(address = "ix")
    public boolean ix = true;
    
    @Map(address = "i")
    public byte[] i = { -127, 34, 21, 127 };
    
    @Map(address = "j")
    public short[] j = { -23444, 5438, 19299 };
    
    @Map(address = "k")
    public int[] k = { -56945432, 1243, 42834828 };
    
    @Map(address = "l")
    public long[] l = { 1273485879348795L, 435349085938L };

    @Map(address = "m")
    public float[] m = { 127.34509F, 127989874.3F, 0.0F, 6.99214843F };

    @Map(address = "n")
    public double[] n = { 1278584.823488902D, 289892.333D, 0.0D, 692269.D };

    @Map(address = "o")
    public boolean[] o = { true, false };

    @Map(address = "pxe")
    public String[] p = { "hello", "world" };
    
    public transient String tA = "transient";
}