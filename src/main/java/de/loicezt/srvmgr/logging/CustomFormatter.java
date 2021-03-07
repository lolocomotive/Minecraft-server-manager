package de.loicezt.srvmgr.logging;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {
    @Override
    public String format(LogRecord logRecord) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy | HH:mm:ss");
        return "[" + sdf.format(new Timestamp(logRecord.getMillis())) + "] [" + logRecord.getLoggerName() + "] [" + logRecord.getLevel().getName() + "] " + logRecord.getMessage() + "\n";
    }
}
