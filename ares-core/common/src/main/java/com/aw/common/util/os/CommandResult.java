package com.aw.common.util.os;


/**
 * Store results of a command
 */
public class CommandResult {

    public Process process;
    public  long pid =0;

    /**
     * @return full stdout of the process after execution
     */
    public String getStdOut() { return stdOut.toString(); }
    public StringBuilder stdOut = new StringBuilder();

    /**
     * @return full stderr of the process after execution
     */
    public String getStdErr() { return stdErr.toString(); }
    public StringBuilder stdErr = new StringBuilder();

    /**
     * @return the return code from the process after execution
     */
    public int getResult() { return result; }
    public int result = -1;

    @Override
    public String toString() {

        StringBuffer ret = new StringBuffer();
       ret.append("PID: " + pid);
        ret.append(" result " + result + "\n");
        ret.append(" OUT:   " + stdOut.toString() + "\n");
        ret.append(" ERR:   " + stdErr.toString() + "\n");

        return ret.toString();
    }

}
