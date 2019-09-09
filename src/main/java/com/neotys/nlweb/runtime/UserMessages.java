package com.neotys.nlweb.runtime;

import java.io.OutputStream;
import java.io.PrintStream;

public class UserMessages {

    private PrintStream messageStream;
    private PrintStream errorStream;

    public UserMessages() {
        messageStream = System.out;
        errorStream = System.err;
    }

    public UserMessages(OutputStream messageStream, OutputStream errorStream) {
        this.messageStream = new PrintStream(messageStream);
        this.errorStream = new PrintStream(errorStream);
    }

    public void printMessage(String message) {
        printMessage(message, true);
    }

    public void printMessage(String message, boolean crlf) {
        if(crlf) messageStream.println(message);
        else messageStream.print(message);
    }

    public void printError(String error) {
        printerror(error, true);
    }

    public void printerror(String error, boolean crlf) {
        if(crlf) errorStream.println(error);
        else errorStream.print(error);
    }

}
