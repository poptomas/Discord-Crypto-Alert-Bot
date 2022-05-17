package cz.cuni.mff.semestral.processor;

/**
 * Data class containing the command name
 * and its description, primarily used
 * for the automation of help command
 */
public class Command {
    public Command(String inName, String inDescription) {
        name = inName;
        description = inDescription;
    }

    public String GetName() {
        return name;
    }

    public String GetDescription() {
        return description;
    }

    public String GetLine() {
        return String.format("%s\n - %s\n", name, description);
    }

    private final String name;
    private final String description;
}
