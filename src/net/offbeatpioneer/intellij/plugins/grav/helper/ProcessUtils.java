package net.offbeatpioneer.intellij.plugins.grav.helper;

//import org.apache.commons.io.*;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.SystemUtils;

import org.apache.commons.lang.SystemUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ProcessUtils
 *
 * @author Dome
 * @since 07.01.2017
 */
public class ProcessUtils {
    String[] commands;
    private File workingDirectory;
    private String errorOutput;
    private String[] output = null;
    StreamGobbler inputStream;
    StreamGobbler errorStream;

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

    private static PrintStream out;

    public Object[] execute() {
//            String[] command = new String[2 + commands.length];
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
//            String[] command = {"CMD", "/C", "RScript", file.getAbsoluteFile().toString(), "--args", fncName, "\"" + json + "\""};
        ProcessBuilder probuilder = new ProcessBuilder(command);
//        probuilder.redirectErrorStream(true);
//        File thisFile = Paths.get(workingDirectory.getAbsolutePath(), "error.txt").toFile();
//        probuilder.redirectError(thisFile);
        //You can set up your work directory
        if (workingDirectory != null)
            probuilder.directory(workingDirectory);

        List<Object> output = new ArrayList<>();


        try {
            Process process = probuilder.start();
            System.out.printf("Output of running %s is:\n", command);
            InputStream errStream = process.getErrorStream();
            InputStream inStream = process.getInputStream();
            OutputStream outStream = process.getOutputStream();
            out = new PrintStream(new BufferedOutputStream(outStream));
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
//                String[] errorLines = StringUtils.split(FileCreateUtil.readFile(thisFile.getPath(), Charset.defaultCharset()), '\n');
//                if (errorLines != null) {
//                    this.errorOutput = Arrays.toString(errorLines);
//                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
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
