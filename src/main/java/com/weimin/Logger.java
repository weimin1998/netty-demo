package com.weimin;

import io.netty.buffer.ByteBuf;

import java.text.SimpleDateFormat;
import java.util.Date;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

public class Logger {

    private static final String INFO = "INFO";
    private static final String DEBUG = "DEBUG";
    private static final String ERROR = "ERROR";
    private static final String WARN = "WARN";

    private final Class clazz;

    public Logger(Class clazz) {
        this.clazz = clazz;
    }

    public void info(Object msg, Object... obj) {
        doLog(INFO, msg, obj);
    }

    public void error(Object msg, Object... obj) {
        doLog(ERROR, msg, obj);
    }

    public void debug(Object msg, Object... obj) {
        doLog(DEBUG, msg, obj);
    }

    public void warn(Object msg, Object... obj) {
        doLog(WARN, msg, obj);
    }
    private String now() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        return dateFormat.format(now);
    }

    private void doLog(String level, Object msg, Object... obj) {
        String result = msg.toString();
        for (Object o : obj) {
            result = result.replaceFirst("[{]", o.toString());
            result = result.replaceFirst("[}]", "");
        }
        System.out.println(now() + " " + level + " ---- [" + Thread.currentThread().getName() + "] " + clazz.getName() + " : " + result);
    }

    public static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf.toString());
    }
}
