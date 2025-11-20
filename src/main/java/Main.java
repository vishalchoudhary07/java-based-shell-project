import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(File.pathSeparator);
        
        Path currentPath = Paths.get("");
        String pwd = currentPath.toAbsolutePath().toString();
        Scanner scanner = new Scanner(System.in);

        repl:
        while (true) {
            System.out.print("$ ");
            if (!scanner.hasNextLine()) break;
            
            String input = scanner.nextLine();
            // Use our new Parser class
            List<String> commands = CommandParser.parse(input);
            
            if (commands.isEmpty()) continue;
            
            String command = commands.get(0);

            if (isBuiltin(command)) {
                Builtin builtin = Builtin.valueOf(command);
                switch (builtin) {
                    case exit:
                        if (commands.size() == 1 || "0".equals(commands.get(1))) 
                            break repl;
                            break;
                            
                    case echo:
                        if (commands.size() > 1) {
                             System.out.println(String.join(" ", commands.subList(1, commands.size())));
                        } else {
                            System.out.println();
                        }
                        break;

                    case type:
                        if (commands.size() < 2) break;
                        String target = commands.get(1);
                        if (isBuiltin(target)) {
                            System.out.printf("%s is a shell builtin%n", target);
                        } else {
                            File found = ProcessRunner.findExecutable(target, paths);
                            if (found != null) System.out.printf("%s is %s%n", target, found.getPath());
                            else System.out.printf("%s: not found%n", target);
                        }
                        break;
                    case pwd:
                        System.out.println(pwd);
                        break;
                    case cd:
                        if (commands.size() < 2) break;
                        try {
                            Path newPath;
                            String dir = commands.get(1);
                            if (dir.startsWith("~")) newPath = Paths.get(System.getenv("HOME"), dir.substring(1));
                            else if (Paths.get(dir).isAbsolute()) newPath = Paths.get(dir);
                            else newPath = Paths.get(pwd, dir);
                            
                            if (Files.isDirectory(newPath)) {
                                pwd = newPath.toRealPath().toAbsolutePath().toString();
                                System.setProperty("user.dir", pwd); 
                            } else {
                                System.out.printf("cd: %s: No such file or directory%n", dir);
                            }
                        } catch (IOException | InvalidPathException e) {
                            System.out.printf("cd: %s: No such file or directory%n", commands.get(1));
                        }
                        break;
                    case cat:
                        if (commands.size() < 2) break;
                        Path fileToRead = Paths.get(pwd, commands.get(1));
                        if (Files.exists(fileToRead) && !Files.isDirectory(fileToRead)) {
                            try { Files.lines(fileToRead).forEach(System.out::println); } 
                            catch (IOException e) { System.out.println("Error reading file: " + e.getMessage()); }
                        } else {
                            System.out.println("cat: " + commands.get(1) + ": No such file");
                        }
                        break;
                }
            } else {
                // Use our new Runner class
                ProcessRunner.runExternal(commands, pwd, paths);
            }
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
}