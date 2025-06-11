import org.dnttr.zephyr.serializer.annotations.Address;
import org.dnttr.zephyr.serializer.annotations.Serializable;

/**
 * @author dnttr
 */

@Serializable
public class SerializableObject {

    @Address(address = "a")
    public byte a = 127;
    
    @Address(address = "b")
    public short b = -128;

    @Address(address = "c")
    public char c = 'c';
    
    @Address(address = "d")
    public final int d;
    
    @Address(address = "e")
    public long e = -9223372036854775807L;
    
    @Address(address = "f")
    public float f = -1.0F;
    
    @Address(address = "g")
    public double g = -1.0D;
    
    @Address(address = "h")
    public String h = "hello";

    @Address(address = "ix")
    public boolean ix = true;
    
    @Address(address = "i")
    public byte[] i = { -127, 34, 21, 127 };
    
    @Address(address = "j")
    public short[] j = { -23444, 5438, 19299 };
    
    @Address(address = "k")
    public int[] k = { -56945432, 1243, 42834828 };
    
    @Address(address = "l")
    public long[] l = { 1273485879348795L, 435349085938L };

    @Address(address = "m")
    public float[] m = { 127.34509F, 127989874.3F, 0.0F, 6.99214843F };

    @Address(address = "n")
    public double[] n = { 1278584.823488902D, 289892.333D, 0.0D, 692269.D };

    @Address(address = "o")
    public boolean[] o = { true, false };

    @Address(address = "pxe")
    public String[] p = { "hello", "world" };
    
    public transient String tA = "transient";

    public SerializableObject(@Address(address = "d") int d) {
        this.d = d;
    }
}