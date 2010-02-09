package test.unit.com.pyxis.cli;

import com.pyxis.cli.CommandLine;
import com.pyxis.cli.args.gnu.GnuParser;
import com.pyxis.cli.option.Option;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.pyxis.cli.option.OptionBuilder.optionNamed;
import static org.junit.Assert.*;

@RunWith(org.jmock.integration.junit4.JMock.class)
public class CommandLineTest {
    private Mockery context = new JUnit4Mockery();
    private CommandLine cl = new CommandLine();

    @Test public void defaultsToNoParameter() {
        assertEquals(0, cl.getOperandCount());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void accessingOutOfRangeOperandsFails() {
        assertNull(cl.getParameter(1));
    }

    @Test public void operandsAreAccessibleByIndex() throws Exception {
        cl.parse(new GnuParser(), "first", "second", "third");
        assertEquals("first", cl.getParameter(0));
        assertEquals("second", cl.getParameter(1));
        assertEquals("third", cl.getParameter(2));
    }

    @Test public void optionsHaveNoValueUnlessGiven() throws Exception {
        cl.addOption(optionNamed("debug").withShortForm("d").make());
        assertFalse(cl.hasOptionValue("debug"));
        assertNull(cl.getSingleOptionValue("debug"));

        cl.parse(new GnuParser(), "-d");
        assertTrue(cl.hasOptionValue("debug"));
        assertEquals(Boolean.TRUE, cl.getSingleOptionValue("debug"));
    }

    @Test public void stubGetsCalledWhenOptionIsGiven() throws Exception {
        final Option.Stub turnDebugOn = context.mock(Option.Stub.class, "turn debug on");
        final Option debug = optionNamed("debug").withShortForm("d").whenPresent(turnDebugOn).make();
        cl.addOption(debug);

        final Option.Stub setLocale = context.mock(Option.Stub.class, "set localeOption");
        final Option locale = optionNamed("localeOption").withShortForm("l").whenPresent(setLocale).make();
        cl.addOption(locale);

        context.checking(new Expectations() {{
            never(turnDebugOn);
            one(setLocale).call(with(equal(locale)));
        }});

        cl.parse(new GnuParser(), "-l");
    }
}
