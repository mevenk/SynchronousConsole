/**
 * 
 */
package mevenk.SynchronousConsole.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Venkatesh
 *
 */
public abstract class SynchronousConsoleUtil {

	public static final String LINE_SEPARATOR = System.lineSeparator();
	public static final String USER_NAME = System.getProperty("user.name");

	private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
	public static final String USER_HOME_DIRECTORY_PATH = System.getProperty("user.home");

	public static int lineCount(File file) {
		int noOfLines = -1;

		try {
			String filePath = file.getPath().replace("\\", "/");

			String command = "";

			if (isWindows()) {
				command = "find /v /c \"\"";
			} else if (isUnix()) {
				command = "wc -l";
			} else {
				return noOfLines;
			}

			Process process = Runtime.getRuntime().exec(command + " " + "\"" + filePath + "\"");

			process.waitFor();

			int exitValue = process.exitValue();

			BufferedReader bufferedReader;
			String outputLine = "";
			StringBuffer outputLineStringBuffer = new StringBuffer(outputLine);

			if (exitValue == 0) {
				bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			} else {
				bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			}

			while ((outputLine = bufferedReader.readLine()) != null) {
				outputLineStringBuffer.append(outputLine);
			}

			if (exitValue != 0) {
				System.err.println("Error finding Line count - Command Exit Value : " + exitValue + LINE_SEPARATOR
						+ outputLineStringBuffer.toString());
				return noOfLines;

			}

			String commandOutputRegex = "";

			if (isWindows()) {
				commandOutputRegex = "-*.*" + filePath.toUpperCase() + ":\\s(\\d+)";
			} else if (isUnix()) {
				commandOutputRegex = "(\\d+)\\s*" + filePath;
			}

			Matcher commandOutputMatcher = Pattern.compile(commandOutputRegex)
					.matcher(outputLineStringBuffer.toString());

			if (commandOutputMatcher.matches()) {
				noOfLines = Integer.parseInt(commandOutputMatcher.group(1));
			}

			return noOfLines;
		} catch (Exception exception) {
			return noOfLines;
		}
	}

	private static boolean isWindows() {

		return (OS_NAME.indexOf("win") >= 0);

	}

	private static boolean isUnix() {

		return (OS_NAME.indexOf("nix") >= 0 || OS_NAME.indexOf("nux") >= 0 || OS_NAME.indexOf("aix") > 0);

	}

}
