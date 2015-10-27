package org.ovirt.vdsm.test;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ovirt.vdsm.jsonrpc.client.ClientConnectionException;
import org.ovirt.vdsm.test.client.ProviderFactory;

public class Runner {

    private DefaultParser parser;
    private Options options;

    public Runner() {
        this.parser = new DefaultParser();
        this.options = new Options();

        prepareOptions();
    }

    public void run(String[] args) throws ClientConnectionException {
        CommandLine line = parse(args);
        if (line != null) {
            try {
                ScenarioLoader loader = new ScenarioLoader(checkIfAvailable(line, "f", null, true));
                ScenarioExecutor executor = new ScenarioExecutor(checkIfAvailable(line, "h", "localhost", false),
                        Integer.parseInt(checkIfAvailable(line, "p", "54321", false)),
                        ProviderFactory.getProvider(checkIfAvailable(line, "s", null, true),
                                checkIfAvailable(line, "l", null, false)));
                executor.submit(loader.getScenario(),
                        Integer.parseInt(checkIfAvailable(line, "n", "1", false)),
                        Integer.parseInt(checkIfAvailable(line, "r", "1", false)),
                        Integer.parseInt(checkIfAvailable(line, "t", "1", false)),
                        checkIfAvailable(line, "m", "/tmp", false));
                executor.close();
            } catch (IOException e) {
                System.err.println("Provided file not found");
                formatHelp();
            } catch (NumberFormatException e) {
                System.err.println("Provided numeric value is not a number");
                formatHelp();
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                formatHelp();
            }
        }
    }

    private CommandLine parse(String[] args) {
        CommandLine line = null;
        try {
            line = parser.parse(this.options, args);
        } catch (ParseException e) {
            formatHelp();
        }
        return line;
    }

    private void formatHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("smoke", this.options);
    }

    private String checkIfAvailable(CommandLine line, String optionName, String defaultValue, boolean failIfEmpty) {
        if (!line.hasOption(optionName) && failIfEmpty) {
            throw new IllegalArgumentException("Option: " + optionName + " is required but not set");
        }
        String value = line.getOptionValue(optionName);
        return (value == null || value.trim().equals("")) ? defaultValue : value;
    }

    private void prepareOptions() {
        this.options.addOption("h", "host", true, "Host where vdsm is running");
        this.options.addOption("p", "port", true, "Port on which vdsm is listening");
        this.options.addOption("f", "path", true, "Path to scenario yaml file");
        this.options.addOption("n", "number-of-threads", true, "Specifies how many threads used during test");
        this.options.addOption("r", "repeat", true, "Specifies how many time repeat scenario");
        this.options.addOption("t", "time", true, "Specify for how long run the test in minutes");
        this.options.addOption("s", "secure", true, "Specify whether to use engine or vdsm config to load certs");
        this.options.addOption("l", "location", true, "Customize location of the config file");
        this.options.addOption("m", "metric", true, "Location of a directory where metrics csv file are stored");
    }

    public static void main(String[] args) throws ClientConnectionException {
        Runner runner = new Runner();
        runner.run(args);
    }
}
