package cz.cuni.mff.semestral.processor;

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
