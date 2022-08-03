package com.example.spring.simple.ioc.core;

import java.io.PrintStream;
import java.io.PrintWriter;

public abstract class NestedCheckedException extends Exception {

    private Throwable cause;

    public NestedCheckedException(String msg) {
        super(msg);
    }

    public NestedCheckedException(String msg, Throwable ex) {
        super(msg);
        this.cause = ex;
    }

    public Throwable getCause() {
        return (cause == this ? null : cause);
    }

    public String getMessage() {
        // Even if you cannot set the cause of this exception other than through
        // the constructor, we check for the cause being "this" here, as the cause
        // could still be set to "this" via reflection: for example, by a remoting
        // deserializer like Hessian's.
        if (this.cause == null || this.cause == this) {
            return super.getMessage();
        } else {
            return super.getMessage() + "; nested exception is " + this.cause.getClass().getName() +
                ": " + this.cause.getMessage();
        }
    }

    public void printStackTrace(PrintStream ps) {
        if (this.cause == null || this.cause == this) {
            super.printStackTrace(ps);
        } else {
            ps.println(this);
            this.cause.printStackTrace(ps);
        }
    }

    public void printStackTrace(PrintWriter pw) {
        if (this.cause == null || this.cause == this) {
            super.printStackTrace(pw);
        } else {
            pw.println(this);
            this.cause.printStackTrace(pw);
        }
    }

}
