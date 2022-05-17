package cz.cuni.mff.semestral.utilities;

/**
 * Simple class to store a key-value pair
 * of an arbitrary type
 * @param <T>
 * @param <U>
 */
public class Pair<T, U> {
    public T first;
    public U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("%s:%20s USD\n", first, second);
    }
}
