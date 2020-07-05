package net.offbeatpioneer.intellij.plugins.grav.helper;

import java.io.*;
import java.util.Scanner;

//https://stackoverflow.com/questions/25878415/java-processbuilder-process-waiting-for-input
class StreamGobbler implements Runnable {
    private PrintStream out;
    //    private Scanner inScanner;
    BufferedReader reader;
    private String name;
    private StringBuilder builder = new StringBuilder("");

    public StreamGobbler(String name, PrintStream out, InputStream inStream) {
        this.name = name;
        this.out = out;
//        inScanner = new Scanner(new BufferedInputStream(inStream));
        reader = new BufferedReader(new InputStreamReader(inStream));
    }

    @Override
    public void run() {
        builder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        boolean entertemplate = false;
//        while (inScanner.hasNextLine()) {
//            String line = inScanner.nextLine();
//            builder.append(line).append("\n");
//
//            System.out.printf("%s: %s%n", name, line);
//        }
    }

    public String getOutput() {
        return builder.toString();
    }
}