import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    // ANSI Color Codes
    public static final String GREEN = "\u001B[32m";
    public static final String RESET = "\u001B[0m";
    public static final String BLUE = "\u001B[34m";

    public static void main(String[] args) {
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(File.pathSeparator);
        
        Path currentPath = Paths.get("");
        String pwd = currentPath.toAbsolutePath().toString();
        Scanner scanner = new Scanner(System.in);
        List<String> history = new ArrayList<>();

        repl:
        while (true) {
            // UX Update: Short Prompt (Folder Name only)
            // Gets the last part of the path (e.g., "java" instead of "C:\Users\...\java")
            String folderName = Paths.get(pwd).getFileName().toString();
            System.out.print(BLUE + folderName + " " + GREEN + "$ " + RESET);
            
            if (!scanner.hasNextLine()) break;
            
            String input = scanner.nextLine();
            if (!input.trim().isEmpty()) {
                history.add(input);
            }

            List<String> commands = CommandParser.parse(input);
            if (commands.isEmpty()) continue;
            
            String command = commands.get(0);

            if (isBuiltin(command)) {
                Builtin builtin = Builtin.valueOf(command);
                switch (builtin) {
                    case exit:
                        if (commands.size() == 1 || "0".equals(commands.get(1))) break repl;
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
                    case history:
                        for (int i = 0; i < history.size(); i++) {
                            System.out.printf("%d %s%n", i + 1, history.get(i));
                        }
                        break;
                    case clear:
                        System.out.print("\033[H\033[2J\033[3J");
                        System.out.flush();
                        break;
                    
                    // New Feature: ls command
                    case ls:
                        File dir = new File(pwd);
                        File[] files = dir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isDirectory()) {
                                    // Print directories in Blue with a trailing slash
                                    System.out.println(BLUE + file.getName() + File.separator + RESET);
                                } else {
                                    System.out.println(file.getName());
                                }
                            }
                        }
                        break;
                }
            } else {
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