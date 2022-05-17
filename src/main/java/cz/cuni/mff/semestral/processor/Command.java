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
        StringBuilder sb = new StringBuilder();
        int offset = 20;
        sb.append(name)
            .append(" - ")
            .append(description)
            .append("\n");
        return sb.toString();
    }

    private final String name;
    private final String description;
}
