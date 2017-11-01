package it.unibo.alchemist.boundary;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Builder class that eases the building of a queue of {@link DrawCommand}s.
 */
public final class CommandQueueBuilder {
    private final Queue<DrawCommand> commandQueue = new LinkedList<>();

    /**
     * Adds a Runnable to the queue to be executed on the JavaFX thread.
     *
     * @param doOnJFXThread the action to do
     * @return this builder
     */
    public CommandQueueBuilder addCommand(final DrawCommand doOnJFXThread) {
        commandQueue.add(doOnJFXThread);
        return this;
    }

    /**
     * Clears the queue.
     *
     * @return this builder
     */
    public CommandQueueBuilder cleanQueue() {
        commandQueue.clear();
        return this;
    }

    /**
     * Builds the queue of commands.
     *
     * @return the queue of commands
     */
    public Queue<DrawCommand> buildCommandQueue() {
        return commandQueue;
    }
}
