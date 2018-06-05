package com.humanharvest.organz.actions;

import com.humanharvest.organz.HistoryItem;

/**
 * Abstract class used to represent reversible actions within the system. An Action should hold references to
 * all objects necessary for its execution. Actions should always be executed by an {@link ActionInvoker}.
 */
public abstract class Action {
    private HistoryItem executeHistoryItem;
    private HistoryItem unExecuteHistoryItem;

    /**
     * Executes the action, causing some change(s) within the system.
     */
    protected abstract void execute();

    /**
     * Reverses the action, causing the direct effects of its execution to be undone.
     */
    protected abstract void unExecute();

    /**
     * Returns a message describing the change(s) that this action makes.
     * @return A descriptive message describing the effects of executing this action.
     */
    public abstract String getExecuteText();

    /**
     * Returns a message describing the change(s) that reversing this action makes.
     * @return A descriptive message describing the effects of undoing this action.
     */
    public abstract String getUnexecuteText();

    /**
     * Returns the lazily-initialised {@link HistoryItem} corresponding to this action being executed.
     * @return The {@link HistoryItem} corresponding to this action's execution.
     */
    protected HistoryItem getExecuteHistoryItem() {
        // Initialise the history item if it hasn't been used already (lazy initialisation)
        if (executeHistoryItem == null) {
            executeHistoryItem = new HistoryItem("ACTION", this.getExecuteText());
        }
        return executeHistoryItem;
    }

    /**
     * Returns the lazily-initialised {@link HistoryItem} corresponding to this action being undone.
     * @return The {@link HistoryItem} corresponding to this action's reversal.
     */
    protected HistoryItem getUnExecuteHistoryItem() {
        // Initialise the history item if it hasn't been used already (lazy initialisation)
        if (unExecuteHistoryItem == null) {
            unExecuteHistoryItem = new HistoryItem("ACTION", this.getUnexecuteText());
        }
        return unExecuteHistoryItem;
    }
}
