package org.alicebot.ab.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;

public class IOUtils {

    private static final Logger log = LoggerFactory.getLogger(IOUtils.class);

    private BufferedReader reader;
    private BufferedWriter writer;

    public IOUtils(String filePath, String mode) {
        try {
            if (mode.equals("read")) {
                reader = new BufferedReader(new FileReader(filePath));
            } else if (mode.equals("write")) {
                if (!new File(filePath).delete()) {
                    log.warn("Could not delete {}", filePath);
                }
                writer = new BufferedWriter(new FileWriter(filePath, true));
            }
        } catch (IOException e) {
            log.warn("IOUtils[path={}, mode={}] init error", filePath, mode, e);
        }
    }

    public String readLine() {
        String result = null;
        try {
            result = reader.readLine();
        } catch (IOException e) {
            log.warn("readLine  error", e);
        }
        return result;
    }

    public void writeLine(String line) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            log.warn("writeLine  error", e);
        }
    }

    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            log.warn("close  error", e);
        }
    }

    public static void writeOutputTextLine(String prompt, String text) {
        log.info("{}: {}", prompt, text);
    }

    public static String readInputTextLine() {
        return readInputTextLine(null);
    }

    public static String readInputTextLine(String prompt) {
        if (prompt != null) {
            log.info("{}: ", prompt);
        }
        BufferedReader lineOfText = new BufferedReader(new InputStreamReader(System.in));
        String textLine = null;
        try {
            textLine = lineOfText.readLine();
        } catch (IOException e) {
            log.error("Error: ", e);
        }
        return textLine;
    }

    public static File[] listFiles(File dir) {
        return dir.listFiles();
    }

    public static String system(String evaluatedContents, String failedString) {
        Runtime runtime = Runtime.getRuntime();
        if (log.isDebugEnabled()) {
            log.debug("System = {}", evaluatedContents);
        }
        try {
            Process process = runtime.exec(evaluatedContents);
            try (InputStreamReader reader = new InputStreamReader(process.getInputStream())) {
                BufferedReader buffer = new BufferedReader(reader);
                StringBuilder result = new StringBuilder();
                String data = "";
                while ((data = buffer.readLine()) != null) {
                    result.append(data).append("\n");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Result = {}", failedString);
                }
                return result.toString();
            }
        } catch (Exception e) {
            log.error("system command execution failed", e);
            return failedString;
        }
    }

    public static String evalScript(String engineName, String script) throws Exception {
        if (log.isDebugEnabled()) {
            log.info("Evaluating script = {}", script);
        }
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName(engineName);
        return "" + engine.eval(script);
    }
}

