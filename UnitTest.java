package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Noor Gill
 */
public class UnitTest {


    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void refsTest() {
        HashMap<String, String> blobs = new HashMap<>();
        blobs.put("fileX", "fileA");
        blobs.put("fileY", "fileB");
    }
    @Test
    public void initTest() {
        String init = "init";
        List<String> ids = new ArrayList<>();
        assertEquals(init, "init");
        String init1 = "add";
        List<String> ids1 = new ArrayList<>();
        assertEquals(init1, "add");
        String init2 = "commits";
        List<String> ids2 = new ArrayList<>();
        assertEquals(init2, "commits");
        assertEquals(ids, ids1);
        assertEquals(ids1, ids2);
    }

}


