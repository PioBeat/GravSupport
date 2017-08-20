package net.offbeatpioneer.intellij.plugins.grav.helper;

import org.apache.commons.lang.SystemUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ProcessUtils for Grav communication
 *
 * @author Dominik Grzelak
 * @since 2017-01-07
 */
public class ProcessUtils {
    private String[] commands;
    private File workingDirectory;
    private String errorOutput;
    private String[] output = null;
    private StreamGobbler inputStream;
    private StreamGobbler errorStream;

    public ProcessUtils() {
        this(new String[]{}, new File(""));
    }

    public ProcessUtils(String[] commands) {
        this(commands, new File(""));
    }

    public ProcessUtils(String[] commands, File workingDirectory) {
        this.commands = commands;
        this.workingDirectory = workingDirectory;
    }

    public String[] getCommands() {
        return commands;
    }

    public void setCommands(String[] commands) {
        this.commands = commands;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public Object[] execute() {
        List<String> command = new ArrayList<>();
        if (SystemUtils.IS_OS_WINDOWS) {
            command.add("CMD");
            command.add("/C");
        } else {
            //TODO check other shells
            command.add("/bin/sh");
            command.add("-c");
        }
        command.addAll(Arrays.asList(commands));
        ProcessBuilder probuilder = new ProcessBuilder(command);
        //You can set up your work directory
        if (workingDirectory != null)
            probuilder.directory(workingDirectory);

        try {
            Process process = probuilder.start();
            System.out.printf("Output of running %s is:\n", command);
            InputStream errStream = process.getErrorStream();
            InputStream inStream = process.getInputStream();
            OutputStream outStream = process.getOutputStream();
            PrintStream out = new PrintStream(new BufferedOutputStream(outStream));
            inputStream = new StreamGobbler("in", out, inStream);
            errorStream = new StreamGobbler("err", out, errStream);
            new Thread(inputStream).start();
            new Thread(errorStream).start();


            int exitValue = process.waitFor();
            System.out.println("\n\nExit Value is " + exitValue);
            if (exitValue == 0) {
                if (inputStream.getOutput().contains("not installed")) {
                    setErrorOutput(inputStream.getOutput());
                } else {
                    setOutput(new String[]{inputStream.getOutput()});
                }
                return getOutput();
            } else {
                BufferedReader errReader = new BufferedReader(new InputStreamReader(errStream));
                StringBuilder linesb = new StringBuilder("");
                String line;
                while ((line = errReader.readLine()) != null) {
                    linesb.append(line).append("\n");
                }
                this.errorOutput = linesb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
    }

    public String[] getOutput() {
        return output;
    }

    public void setOutput(String[] output) {
        this.output = output;
    }

    public String getOutputAsString() {
        if (output == null || output.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String each : output) {
            builder.append(each).append("\n");
        }
        return builder.toString();
    }
}
