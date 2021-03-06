package com.vtence.cli.gnu;

import org.junit.Test;
import com.vtence.cli.ParsingException;
import com.vtence.cli.args.Help;
import com.vtence.cli.args.Operand;
import com.vtence.cli.args.Option;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GnuHelpTest
{
    String NL = System.getProperty("line.separator");
    GnuHelp help = new GnuHelp(20);

    @Test public void
    displaysEmptyUsageByDefault() throws IOException {
        assertHelp("Usage:");
    }

    @Test public void
    describesPositionalArguments() throws IOException {
        help.add(Operand.named("input").as("IN"));
        help.add(Operand.named("output").as("OUT").describedAs("The destination file"));
        assertHelp(
                "Usage: IN OUT",
                "",
                "Arguments:",
                "IN                  ",
                "OUT                  The destination file"
        );
    }

    @Test public void
    wrapsDescriptionOfVeryLongPositionalArguments() throws IOException {
        help.add(Operand.named("input").as("A VERY LONG POSITIONAL ARGUMENT").describedAs("The source file"));
        assertHelp(
                "Usage: A VERY LONG POSITIONAL ARGUMENT",
                "",
                "Arguments:",
                "A VERY LONG POSITIONAL ARGUMENT\n" +
                "                     The source file"
        );
    }
    @Test public void
    startsWithProgramNameIfSpecified() throws IOException {
        help.setProgram("program");
        assertHelp(
                "program",
                "",
                "Usage: program");
    }

    @Test public void
    displaysVersionAlongProgramNameWhenGiven() throws IOException {
        help.setProgram("program");
        help.setVersion("1.0");
        assertHelp(
                "program version 1.0",
                "",
                "Usage: program");
    }


    @Test public void
    descriptionFollowsProgramNameWhenGiven() throws IOException {
        help.setProgram("program");
        help.setDescription("My cool program.");
        assertHelp(
                "program",
                "",
                "My cool program.",
                "",
                "Usage: program");
    }

    @Test public void
    includesDescriptionOfOptions() throws ParsingException, IOException {
        help.add(Option.flag("--raw"));
        help.add(Option.option("-b").alias("--block-size").takingArgument("SIZE").describedAs("Specifies block size"));
        help.add(Option.flag("-x").describedAs("Turn debugging on"));

        assertHelp(
                "Usage: [--raw] [-b SIZE] [-x]",
                "",
                "Options:",
                "    --raw           ",
                "-b, --block-size SIZE",
                "                     Specifies block size",
                "-x                   Turn debugging on");
    }

    @Test public void
    endsWithEpilog() throws ParsingException, IOException {
        help.setEnding("use \"program --help\" to get help");
        help.add(Option.flag("-x").describedAs("Turn debugging on"));
        assertHelp(
                "Usage: [-x]",
                "",
                "Options:",
                "-x                   Turn debugging on",
                "",
                "use \"program --help\" to get help");
    }

    private void assertHelp(String... lines) throws IOException {
        StringBuilder expected = new StringBuilder();
        for (String line : lines) {
            expected.append(line).append(NL);
        }
        assertEquals("help", expected.toString(), format(help));
    }

    private String format(Help help) throws IOException {
        StringBuilder output = new StringBuilder();
        help.printTo(output);
        return output.toString();
    }
}
