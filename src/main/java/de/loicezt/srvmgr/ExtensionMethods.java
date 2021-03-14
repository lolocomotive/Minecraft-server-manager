package de.loicezt.srvmgr;

import de.loicezt.srvmgr.logging.CustomFormatter;
import de.loicezt.srvmgr.logging.HTMLFormatter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.*;

/**
 * Contains methods to copy, recursively delete files, or to log stuff
 */
public class ExtensionMethods {

    private static Logger logger = Logger.getLogger(ExtensionMethods.class.getName());

    {
        try {
            setupLogging(logger);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy a file
     * @param from the source file
     * @param to the destination file
     * @throws IOException If the copy fails
     */
    public static void copyFile(File from, File to) throws IOException {
        if (!from.exists()) throw new IOException();
        try (InputStream in = new BufferedInputStream(new FileInputStream(from));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(to))) {

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        }
    }

    /**
     * Method to call when an exception is thrown
     *
     * @param e The exception that occurred
     */
    public static void handleException(Exception e) {
        logger.severe("An error occurred!");
        logger.severe(e.getLocalizedMessage());
        System.exit(1);
    }

    /**
     * Method to easily send a Message on the desired topic
     *
     * @param topic   The MQTT topic
     * @param message The message to send
     * @param client  The MQTT client
     * @throws MqttException If the message fails to send
     */
    public static void mqttMsgSend(String topic, String message, MqttClient client) throws MqttException {
        MqttMessage m = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
        m.setQos(2);
        client.publish(topic, m);
    }

    /**
     * Removes all the files in the array, called at the start and end of program to clean up stuff
     *
     * @param garbage files to be removed
     */
    public static void cleanup(File[] garbage) {
        for (File file : garbage) {
            try {
                if (file.isDirectory()) {
                    deleteDirectory(file.toPath());
                } else {
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the temporary garbage files that should be deleted and are no longer required
     *
     * @return The temporary garbage files that should be deleted and are no longer required
     */
    public static File[] getGarbage() {
        File[] files = new File(".").listFiles();
        List<File> r = new ArrayList<>();
        for (File file : files) {
            if (file.getName().endsWith("1883") || file.getName().startsWith("hs_err_pid")) {
                r.add(file);
            }
        }
        File[] re = new File[r.size()];
        for (int i = 0; i < r.size(); i++) {
            re[i] = r.get(i);
        }
        return re;
    }

    /**
     * Deletes a directory
     *
     * @param dir The directory to be deleted
     * @return Whether the deletion was successful or not
     * @throws IOException If the deletion fails
     */
    public static boolean deleteDirectory(Path dir) throws IOException {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        return !dir.toFile().exists();
    }

    /**
     * Sets up the logger to format the console output correctly and log as Txt and HTML to files
     *
     * @param logger the logger to be set up
     * @throws IOException If the log files couldn't be created / written to
     */
    static public void setupLogging(Logger logger) throws IOException {


        // suppress the logging output to the console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        try {
            if (handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }
        new File("logs").mkdir();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Handler logConsole = new de.loicezt.srvmgr.logging.ConsoleHandler();

        Handler fileTxt = new FileHandler("logs/" + timestamp + ".log");
        Handler fileHTML = new FileHandler("logs/" + timestamp + ".html");

        Level level = Level.parse(Main.config.getLogLevel());
        logger.setLevel(level);
        logConsole.setLevel(level);

        Formatter formatterTxt = new CustomFormatter();
        Formatter formatterHTML = new HTMLFormatter();

        logConsole.setFormatter(formatterTxt);
        fileTxt.setFormatter(formatterTxt);
        fileHTML.setFormatter(formatterHTML);

        logger.addHandler(fileTxt);
        logger.addHandler(fileHTML);
        logger.addHandler(logConsole);


    }
}
