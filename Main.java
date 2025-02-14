import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(":");
        Path currentPath = Paths.get("");
        String pwd = currentPath.toAbsolutePath().toString();

        repl:
        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            List<String> commands = parseCommand(input);
            String command = commands.get(0);
            Map<String, File> scripts = new HashMap<>();
            for (String path : paths) {
                try (Stream<Path> directories = Files.walk(Paths.get(path))) {
                    directories
                            .forEach(file -> scripts.put(String.valueOf(file.getFileName()), file.toFile()));
                } catch (Exception _) {

                }
            }
            if (isBuiltin(command)) {
                Builtin builtin = Builtin.valueOf(command);
                switch (builtin) {
                    case exit: {
                        if ("0".equals(commands.get(1)))
                            break repl;
                    }

                    case type: {
                        if (isBuiltin(commands.get(1)) || "echo".equals(commands.get(1))) {
                            System.out.printf("%s is a shell builtin%n", commands.get(1));
                        } else if (scripts.containsKey(commands.get(1))) {
                            System.out.printf("%s is %s%n", commands.get(1), scripts.get(commands.get(1)).getPath());
                        } else {
                            System.out.printf("%s: not found%n", commands.get(1));
                        }
                        break;
                    }

                    case pwd: {
                        System.out.println(pwd);
                        break;
                    }

                    case cd: {
                        try {
                            Path newPath;
                            if (commands.get(1).charAt(0) == '~') {
                                newPath = Paths.get(System.getenv("HOME"), commands.get(1).substring(1));
                            } else if (commands.get(1).charAt(0) == '/')
                                newPath = Paths.get(commands.get(1));
                            else
                                newPath = Paths.get(pwd, commands.get(1));
                            if (Files.notExists(newPath)) {
                                System.out.printf("cd: %s: No such file or directory%n", commands.get(1));
                            } else {
                                pwd = newPath.toRealPath().toAbsolutePath().toString();
                            }
                        } catch (InvalidPathException e) {
                            System.out.printf("cd: %s: No such file or directory%n", commands.get(1));
                        }
                        break;
                    }
                }
            } else if ("echo".equals(command)) {
                List<String> echoContent = new ArrayList<>();
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.inheritIO();
                int x = 1;  // Start from 1 to skip the "echo" command
                while (x < commands.size()) {
                    if (commands.get(x).equals(">") || commands.get(x).equals("1>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectOutput(file);
                        break;
                    } else if (commands.get(x).equals(">>") || commands.get(x).equals("1>>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(file));
                        break;
                    } else if (commands.get(x).equals("2>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectError(file);
                        break;
                    } else if (commands.get(x).equals("2>>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(file));
                        break;
                    } else {
                        echoContent.add(commands.get(x));
                    }
                    x++;
                }
                if (processBuilder.redirectOutput() == ProcessBuilder.Redirect.INHERIT) {
                    // If no redirection, print normally to console
                    System.out.println(String.join(" ", echoContent));
                } else {
                    // If output redirection is detected, use cmd.exe /c echo
                    List<String> command_list = new ArrayList<>();
                    command_list.add("cmd.exe");
                    command_list.add("/c");
                    command_list.add("echo");
                    command_list.add(String.join(" ", echoContent));
                    processBuilder.command(command_list);
                    Process process = processBuilder.start();
                    process.waitFor();
                }
                
            } else if (scripts.containsKey(command)) {
                List<String> undirected = new ArrayList<>();
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.inheritIO();
                int x = 0;
                while (x < commands.size()) {
                    if (commands.get(x).equals(">") || commands.get(x).equals("1>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectOutput(file);
                        break;
                    } else if (commands.get(x).equals(">>") || commands.get(x).equals("1>>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(file));
                        break;
                    } else if (commands.get(x).equals("2>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectError(file);
                        break;
                    } else if (commands.get(x).equals("2>>")) {
                        File file = new File(commands.get(x + 1));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(file));
                        break;
                    } else {
                        undirected.add(commands.get(x));
                    }
                    x++;
                }

                if ("ls".equals(command)) {
                    processBuilder.command(command, pwd);
                }
                processBuilder.command(undirected);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
            } else {
                System.out.printf("%s: command not found%n", command);
            }
        }
    }

    private static boolean isBuiltin(String command) {
        try {
            Builtin.valueOf(command);
            return true;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }

    private static List<String> parseCommand(String input) {
        List<String> arg = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean isEscaped = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == '\'') {
                if (!isEscaped && !inDoubleQuotes) {
                    inSingleQuotes = !inSingleQuotes;
                } else {
                    currentArg.append(c);
                    isEscaped = false;
                }
            } else if (c == '"') {
                if (!isEscaped && !inSingleQuotes) {
                    inDoubleQuotes = !inDoubleQuotes;
                } else {
                    currentArg.append(c);
                    isEscaped = false;
                }
            } else if (c == '\\') {
                if (inSingleQuotes) {
                    currentArg.append(c);
                } else if (isEscaped) {
                    currentArg.append(c);
                    isEscaped = false;
                } else {
                    isEscaped = true;
                }
            } else if (c == ' ') {
                if (inSingleQuotes || inDoubleQuotes || isEscaped) {
                    if (isEscaped) isEscaped = false;
                    currentArg.append(c);
                } else if (currentArg.length() > 0) {
                    arg.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
            } else {
                if (isEscaped && !inSingleQuotes) {
                    isEscaped = false;
                    currentArg.append('\\');
                }
                currentArg.append(c);
            }
        }
        
        if (currentArg.length() > 0) {
            arg.add(currentArg.toString());
        }
        
        return arg;
    }
}

enum Builtin {
    exit,
    type,
    pwd,
    cd
}