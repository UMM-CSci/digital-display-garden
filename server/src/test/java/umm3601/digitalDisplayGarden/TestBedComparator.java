package umm3601.digitalDisplayGarden;

import com.google.zxing.WriterException;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by frazi177 on 4/13/17.
 */
public class TestBedComparator
{

    @Test
    public void TestComparator() throws IOException, WriterException {

        String[] exampleArr1 = new String[]{"1N", "1"};
        String[] exampleArr2 = new String[]{"2", "1N", "1"};
        String[] exampleArr3 = new String[]{"1W", "1N", "1", "1E", "1S"};
        String[] exampleArr4 = new String[]{"LG", "2"};
        String[] exampleArr5 = new String[]{"NW", "NE"};

        String[] sortedArr1 = new String[]{"1", "1N"};
        String[] sortedArr2 = new String[]{"1", "1N", "2"};
        String[] sortedArr3 = new String[]{"1", "1N", "1E", "1S", "1W"};
        String[] sortedArr4 = new String[]{"2", "LG"};
        String[] sortedArr5 = new String[]{"NE", "NW"};

        List<String> arr1 = Arrays.asList(exampleArr1);
        List<String> arr2 = Arrays.asList(exampleArr2);
        List<String> arr3 = Arrays.asList(exampleArr3);
        List<String> arr4 = Arrays.asList(exampleArr4);
        List<String> arr5 = Arrays.asList(exampleArr5);

        BedComparator cmp = new BedComparator();

        arr1.sort(cmp);
        assertArrayEquals("1 Doesn't occur before 1N", arr1.toArray(new String[exampleArr1.length]),sortedArr1);

        arr2.sort(cmp);
        assertArrayEquals("2 Doesn't occur after 1", arr2.toArray(new String[exampleArr2.length]),sortedArr2);

        arr3.sort(cmp);
        assertArrayEquals("1N,1E,1S,1W Doesn't sort properly", arr3.toArray(new String[exampleArr3.length]),sortedArr3);

        arr4.sort(cmp);
        assertArrayEquals("2 Doesn't occur after LG", arr4.toArray(new String[exampleArr4.length]),sortedArr4);

        arr5.sort(cmp);
        assertArrayEquals("NE Doesn't occur before NW", arr5.toArray(new String[exampleArr5.length]),sortedArr5);

    }

}
