package reactives4j.core;

/**
 * Common handle interface to a reactive node.
 */
public interface Handle {

    /**
     * Gets the associated reactive context.
     */
    Context getContext();

    /**
     * Disposes of the reactive node.
     */
    void dispose();

}
