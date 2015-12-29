package nl.guno.collab.intellij;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class Environment {

	private static final int EXIT_STATUS_SUCCESS = 0;

    // TODO: Shouldn't we use the user-defined host? (from IntelliCollabConfigurationForm).
//	private static final String HOST = "codecollaborator.quinity.net";
	private static final String HOST = "127.0.0.1";
    static final String REQUIRED_SVN_VERSION = "1.6";

    private String output;

    public boolean checkConnection() throws InterruptedException {
		return exec("ping -n 1 " + HOST);
	}

    public void checkSVNExecutable() throws InterruptedException, SVNWrongVersionException, SVNNotAvailableException {
        if (!exec("svn --version")) {
            throw new SVNNotAvailableException();
        }

        if (!output.contains(REQUIRED_SVN_VERSION)) {
            throw new SVNWrongVersionException();
        }
    }

    private boolean exec(@NotNull String command) {
        output = null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		CommandLine cmdLine = CommandLine.parse(command);
		DefaultExecutor executor = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(5000);
		executor.setWatchdog(watchdog);
		try {
			int exitStatus = executor.execute(cmdLine);
            output = outputStream.toString();
            return EXIT_STATUS_SUCCESS == exitStatus;

		} catch (IOException e) {
			return false;
		}
	}

    public static void main(String[] args) throws InterruptedException {
        try {
            new Environment().checkSVNExecutable();
            System.out.println("Correct version installed.");
        } catch (SVNNotAvailableException e) {
            System.err.println(MessageResources.message("action.error.svnNotAvailable.text", REQUIRED_SVN_VERSION));
        } catch (SVNWrongVersionException e) {
            System.err.println(MessageResources.message("action.error.svnWrongVersion.text", REQUIRED_SVN_VERSION));
        }
    }

    public class SVNNotAvailableException extends Exception {}
    public class SVNWrongVersionException extends Exception {}
}