package soze.serverrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * A simple application for use with Jenkins.
 * After copying files to remote machine,
 * running the actual backend application does not let
 * jenkins finish the build. So we need a separate process
 * which will start the server, output any errors,
 * but let jenkins finish.
 */
public class ServerRunner {

	public static void main(String[] args) {

		System.out.println("This app requires the following arguments:");
		System.out.println("1. Path to the jar file.");
		System.out.println("2. Name of the jar file.");
		System.out.println("e.g. java jar runner.jar /life multilife.jar");


		if (args.length == 0) {
			throw new IllegalArgumentException("Arguments cannot be empty.");
		}

		if (args.length != 2) {
			throw new IllegalArgumentException("Invalid number of arguments.");
		}

		System.out.println("Given following arguments: " + Arrays.toString(args));
		start(args[0], args[1]);
	}

	private static void start(String path, String fileName) {

		ProcessBuilder pb = new ProcessBuilder("java", "-jar", fileName);
		pb.directory(new File(path));

		Process p = null;

		try {
			p = pb.start();
			System.out.println("Process started");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(5);
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			long timeToWait = 1000 * 15;
			long totalTime = 0;
			long startTime = System.currentTimeMillis();
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				totalTime = System.currentTimeMillis() - startTime;
				if(totalTime > timeToWait) {
					System.exit(0);
				}
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.exit(5);
	}

}
