import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // 1. Fix: Use File.pathSeparator for cross-platform compatibility
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(File.pathSeparator);
        
        Path currentPath = Paths.get("");
        String pwd = currentPath.toAbsolutePath().toString();
        
        // 2. Optimization: Move Scanner outside the loop
        Scanner scanner = new Scanner(System.in);

        repl:
        while (true) {
            System.out.print("$ ");
            // Handle Ctrl+D (EOF) gracefully
            if (!scanner.hasNextLine()) break;
            
            String input = scanner.nextLine();
            List<String> commands = parseCommand(input);
            
            if (commands.isEmpty()) continue; // Handle empty input
            
            String command = commands.get(0);

            if (isBuiltin(command)) {
                Builtin builtin = Builtin.valueOf(command);
                switch (builtin) {
                    case exit: {
                        if (commands.size() > 1 && "0".equals(commands.get(1)))
                            break repl;
                        break; // Break switch, not loop
                    }
                    case type: {
                        if (commands.size() < 2) break;
                        String target = commands.get(1);
                        if (isBuiltin(target)) {
                            System.out.printf("%s is a shell builtin%n", target);
                        } else {
                            File found = findExecutable(target, paths);
                            if (found != null) {
                                System.out.printf("%s is %s%n", target, found.getPath());
                            } else {
                                System.out.printf("%s: not found%n", target);
                            }
                        }
                        break;
                    }
                    case pwd: {
                        System.out.println(pwd);
                        break;
                    }
                    case cd: {
                        if (commands.size() < 2) break;
                        try {
                            Path newPath;
                            String dir = commands.get(1);
                            if (dir.startsWith("~")) {
                                newPath = Paths.get(System.getenv("HOME"), dir.substring(1));
                            } else if (Paths.get(dir).isAbsolute()) {
                                newPath = Paths.get(dir);
                            } else {
                                newPath = Paths.get(pwd, dir);
                            }
                            
                            if (Files.isDirectory(newPath)) {
                                pwd = newPath.toRealPath().toAbsolutePath().toString();
                                // Update user.dir so external commands run in the right place
                                System.setProperty("user.dir", pwd); 
                            } else {
                                System.out.printf("cd: %s: No such file or directory%n", dir);
                            }
                        } catch (IOException | InvalidPathException e) {
                            System.out.printf("cd: %s: No such file or directory%n", commands.get(1));
                        }
                        break;
                    }
                    // 3. Feature: Added 'cat' command
                    case cat: {
                        if (commands.size() < 2) break;
                        Path fileToRead = Paths.get(pwd, commands.get(1));
                        if (Files.exists(fileToRead) && !Files.isDirectory(fileToRead)) {
                            try {
                                Files.lines(fileToRead).forEach(System.out::println);
                            } catch (IOException e) {
                                System.out.println("Error reading file: " + e.getMessage());
                            }
                        } else {
                            System.out.println("cat: " + commands.get(1) + ": No such file");
                        }
                        break;
                    }
                }
            } else {
                // Check for external executable
                File executable = findExecutable(command, paths);
                if (executable != null) {
                    executeExternal(commands, executable, pwd);
                } else {
                    System.out.printf("%s: command not found%n", command);
                }
            }
        }
    }

    // Helper to find executable in PATH (Replacing the slow Files.walk)
    private static File findExecutable(String command, String[] paths) {
        for (String path : paths) {
            File file = new File(path, command);
            // On Windows, we might need to check extensions like .exe, .bat, .cmd
            if (file.exists() && file.canExecute()) {
                return file;
            }
            // Optional: Check for Windows extensions if file not found
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                 File exeFile = new File(path, command + ".exe");
                 if (exeFile.exists()) return exeFile;
            }
        }
        return null;
    }

    private static void executeExternal(List<String> commands, File executable, String pwd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.directory(new File(pwd)); // Set the working directory!
            pb.redirectErrorStream(true); // Combine stderr and stdout
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            System.out.printf("Error executing command: %s%n", e.getMessage());
        }
    }

    private static boolean isBuiltin(String command) {
        try {
            Builtin.valueOf(command);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // (Keep your existing parseCommand method here, it was fine!)
    private static List<String> parseCommand(String input) {
        List<String> arg = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean isEscaped = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\'') {
                if (!isEscaped && !inDoubleQuotes) inSingleQuotes = !inSingleQuotes;
                else { currentArg.append(c); isEscaped = false; }
            } else if (c == '"') {
                if (!isEscaped && !inSingleQuotes) inDoubleQuotes = !inDoubleQuotes;
                else { currentArg.append(c); isEscaped = false; }
            } else if (c == '\\') {
                if (inSingleQuotes) currentArg.append(c);
                else if (isEscaped) { currentArg.append(c); isEscaped = false; }
                else isEscaped = true;
            } else if (c == ' ') {
                if (inSingleQuotes || inDoubleQuotes || isEscaped) {
                    if (isEscaped) isEscaped = false;
                    currentArg.append(c);
                } else if (currentArg.length() > 0) {
                    arg.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
            } else {
                if (isEscaped && !inSingleQuotes) { isEscaped = false; currentArg.append('\\'); }
                currentArg.append(c);
            }
        }
        if (currentArg.length() > 0) arg.add(currentArg.toString());
        return arg;
    }
}

enum Builtin {
    exit,
    type,
    pwd,
    cd,
    cat  // Added cat here
}