import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProcessRunner {

    public static void runExternal(List<String> commands, String pwd, String[] paths) {
        // 1. Check for Redirection (>, >>)
        File outputFile = null;
        boolean append = false;
        int redirectIndex = -1;

        // Scan for ">" or ">>" tokens
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).equals(">")) {
                if (i + 1 < commands.size()) {
                    outputFile = new File(pwd, commands.get(i + 1));
                    redirectIndex = i;
                    append = false; // Overwrite
                    break;
                }
            } else if (commands.get(i).equals(">>")) {
                if (i + 1 < commands.size()) {
                    outputFile = new File(pwd, commands.get(i + 1));
                    redirectIndex = i;
                    append = true; // Append
                    break;
                }
            }
        }

        // 2. Separate the command from the redirection parts
        // If we found ">", the actual command is everything BEFORE it.
        List<String> commandToRun;
        if (redirectIndex != -1) {
            commandToRun = commands.subList(0, redirectIndex);
        } else {
            commandToRun = commands;
        }

        if (commandToRun.isEmpty()) return;

        // 3. Find executable and Run
        String command = commandToRun.get(0);
        File executable = findExecutable(command, paths);

        if (executable != null) {
            try {
                ProcessBuilder pb = new ProcessBuilder(commandToRun);
                pb.directory(new File(pwd));
                
                // Handle Output Redirection
                if (outputFile != null) {
                    if (append) {
                        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(outputFile));
                    } else {
                        pb.redirectOutput(ProcessBuilder.Redirect.to(outputFile));
                    }
                } else {
                    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                }

                // We usually inherit input/error, but error might go to file too in real shells
                pb.redirectErrorStream(true); 
                pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                
                Process process = pb.start();
                process.waitFor();
            } catch (Exception e) {
                System.out.printf("Error executing command: %s%n", e.getMessage());
            }
        } else {
            System.out.printf("%s: command not found%n", command);
        }
    }

    public static File findExecutable(String command, String[] paths) {
        for (String path : paths) {
            File file = new File(path, command);
            if (file.exists() && file.canExecute()) {
                return file;
            }
            // Windows fallback check
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                 File exeFile = new File(path, command + ".exe");
                 if (exeFile.exists()) return exeFile;
                 File batFile = new File(path, command + ".bat"); // Also check .bat
                 if (batFile.exists()) return batFile;
                 File cmdFile = new File(path, command + ".cmd"); // Also check .cmd
                 if (cmdFile.exists()) return cmdFile;
            }
        }
        return null;
    }
}