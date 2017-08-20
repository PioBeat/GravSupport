package net.offbeatpioneer.intellij.plugins.grav.helper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

//https://stackoverflow.com/questions/25878415/java-processbuilder-process-waiting-for-input
class StreamGobbler implements Runnable {
    private PrintStream out;
    private Scanner inScanner;
    private String name;
    private StringBuilder builder = new StringBuilder("");

    public StreamGobbler(String name, PrintStream out, InputStream inStream) {
        this.name = name;
        this.out = out;
        inScanner = new Scanner(new BufferedInputStream(inStream));
    }

    @Override
    public void run() {
        boolean entertemplate = false;
        while (inScanner.hasNextLine()) {
            String line = inScanner.nextLine();
            builder.append(line).append("\n");

            System.out.printf("%s: %s%n", name, line);
        }
    }

    public String getOutput() {
        return builder.toString();
    }
}