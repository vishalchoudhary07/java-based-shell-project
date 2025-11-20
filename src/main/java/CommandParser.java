import java.util.ArrayList;
import java.util.List;

public class CommandParser {
    public static List<String> parse(String input) {
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