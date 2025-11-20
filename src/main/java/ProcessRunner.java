import java.io.File;
import java.util.List;

public class ProcessRunner {

    public static void runExternal(List<String> commands, String pwd, String[] paths) {
        String command = commands.get(0);
        File executable = findExecutable(command, paths);

        if (executable != null) {
            try {
                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.directory(new File(pwd));
                pb.redirectErrorStream(true);
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
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
            }
        }
        return null;
    }
}