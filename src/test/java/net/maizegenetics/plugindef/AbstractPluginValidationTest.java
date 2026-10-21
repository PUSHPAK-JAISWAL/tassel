package net.maizegenetics.plugindef;

import com.google.common.collect.Range;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.swing.*;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Parameter validation failures in non-interactive plugins must throw instead
 * of calling System.exit, which would kill the test JVM.
 */
public class AbstractPluginValidationTest {

    @Rule
    public TemporaryFolder myTempFolder = new TemporaryFolder();

    public static class ValidatingPlugin extends AbstractPlugin {

        private PluginParameter<String> myInput = new PluginParameter.Builder<>("i", null, String.class)
                .description("Input file")
                .inFile()
                .required(true)
                .build();

        private PluginParameter<String> myOutputDir = new PluginParameter.Builder<>("outDir", null, String.class)
                .description("Output directory")
                .outDir()
                .build();

        private PluginParameter<Integer> myCount = new PluginParameter.Builder<>("count", 1, Integer.class)
                .description("Count")
                .range(Range.closed(1, 10))
                .build();

        public ValidatingPlugin() {
            super(null, false);
        }

        @Override
        public DataSet processData(DataSet input) {
            return new DataSet(new Datum("result", "ok", null), this);
        }

        @Override
        public ImageIcon getIcon() {
            return null;
        }

        @Override
        public String getButtonName() {
            return "Validating Plugin";
        }

        @Override
        public String getToolTipText() {
            return "Validating Plugin";
        }

    }

    private static IllegalArgumentException expectIllegalArgument(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException e) {
            return e;
        }
        fail("Expected IllegalArgumentException");
        return null;
    }

    @Test
    public void testMissingInputFileThrows() {
        String missing = new File(myTempFolder.getRoot(), "does_not_exist.txt").getPath();
        ValidatingPlugin plugin = new ValidatingPlugin();
        plugin.setParameter("i", missing);

        IllegalArgumentException e = expectIllegalArgument(() -> plugin.performFunction(null));
        assertTrue(e.getMessage(), e.getMessage().contains(missing));
        assertTrue(e.getMessage(), e.getMessage().contains("doesn't exist"));
    }

    @Test
    public void testMissingRequiredParameterThrows() {
        IllegalArgumentException e = expectIllegalArgument(() -> new ValidatingPlugin().performFunction(null));
        assertEquals("-i is required.", e.getMessage());
    }

    @Test
    public void testMissingDirectoryThrows() throws Exception {
        File input = myTempFolder.newFile("input.txt");
        String missingDir = new File(myTempFolder.getRoot(), "no_such_dir").getPath();
        ValidatingPlugin plugin = new ValidatingPlugin();
        plugin.setParameter("i", input.getPath());
        plugin.setParameter("outDir", missingDir);

        IllegalArgumentException e = expectIllegalArgument(() -> plugin.performFunction(null));
        assertTrue(e.getMessage(), e.getMessage().contains(missingDir));
    }

    @Test
    public void testValidParametersRun() throws Exception {
        File input = myTempFolder.newFile("input.txt");
        ValidatingPlugin plugin = new ValidatingPlugin();
        plugin.setParameter("i", input.getPath());

        DataSet result = plugin.performFunction(null);
        assertNotNull(result);
        assertEquals("ok", result.getData(0).getData());
    }

    @Test
    public void testSetParameterOutOfRangeThrows() {
        expectIllegalArgument(() -> new ValidatingPlugin().setParameter("count", "11"));
    }

    @Test
    public void testSetParametersUnrecognizedArgumentThrows() {
        IllegalArgumentException e = expectIllegalArgument(
                () -> new ValidatingPlugin().setParameters(new String[]{"-bogus", "value"}));
        assertEquals("Unrecognized argument: -bogus", e.getMessage());
    }

    @Test
    public void testSetParametersMissingValueThrows() {
        IllegalArgumentException e = expectIllegalArgument(
                () -> new ValidatingPlugin().setParameters(new String[]{"-i"}));
        assertEquals("Parameter requires a value: -i", e.getMessage());
    }

}
