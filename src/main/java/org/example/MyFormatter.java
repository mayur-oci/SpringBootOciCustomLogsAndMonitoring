package org.example;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {


        return "eMedia Video Streaming" + "::"
                + new Date(record.getMillis()) + "::"
                + record.getMessage() + "\n";
//        return record.getThreadID()+"::"+record.getSourceClassName()+"::"
//                +record.getSourceMethodName()+"::"
//                +new Date(record.getMillis())+"::"
//                +record.getMessage()+"\n";
    }

}
