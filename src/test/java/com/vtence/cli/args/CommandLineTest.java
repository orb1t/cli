package com.vtence.cli.args;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import com.vtence.cli.ParsingException;
import com.vtence.cli.coercion.LocaleCoercer;
import com.vtence.cli.gnu.GnuParser;

import java.util.Locale;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CommandLineTest {
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    CommandLine cl = new CommandLine(new GnuParser());

    @Test public void
    startsWithoutArgument() throws ParsingException {
        Args args = cl.parse();
        assertEquals(0, args.size());
    }

    @Test public void
    detectsSpecifiedOptionsWhenPresent() throws ParsingException {
        cl.add(Option.flag("-x"));
        cl.add(Option.flag("-v"));

        Args args = cl.parse("-x");
        assertTrue(args.has("-x"));
        assertEquals(TRUE, args.get("-x"));
        assertFalse(args.has("-v"));
        assertNull(args.get("-v"));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    triggersActionsOnDetectedOptions() throws Exception {
        final Option.Action<Boolean> turnDebugOn = context.mock(Option.Action.class, "turn debug on");
        cl.add(Option.flag("-d").whenPresent(turnDebugOn));

        final Option.Action<Locale> setLocale = context.mock(Option.Action.class, "set locale");
        cl.add(Option.option("-l").takingArgument("LOCALE").ofType(new LocaleCoercer()).whenPresent(setLocale));

        context.checking(new Expectations() {{
            never(turnDebugOn);
            oneOf(setLocale).call(with(any(Args.class)), with(any(Option.class)));
        }});

        cl.parse("-l", "en");
    }

    @Test public void
    detectsSpecifiedOperandsWhenPresent() throws Exception {
        cl.add(Operand.named("input"));
        cl.add(Operand.named("output"));

        Args args = cl.parse("input", "output");

        assertEquals("input", args.get("input"));
        assertEquals("output", args.get("output"));
    }

    @Test public void
    detectsBothOptionsAndOperands() throws Exception {
        cl.add(Option.flag("-v"));
        cl.add(Operand.named("input"));

        Args args = cl.parse("-v", "input");
        assertEquals(2, args.size());
    }

    @Test public void
    complainsWhenRequiredOperandsAreMissing() throws Exception {
        cl.add(Operand.named("input"));
        cl.add(Operand.named("output"));

        try {
            cl.parse("input");
            fail("Expected exception " + MissingOperandException.class.getName());
        } catch (MissingOperandException expected) {
            assertEquals("output", expected.getMissingOperand());
        }
    }

    @Test public void
    returnsUnprocessedArguments() throws Exception {
        cl.add(Operand.named("input"));

        Args args = cl.parse("input", "output");
        assertEquals(asList("output"), args.others());
    }
}
