package com.humanharvest.organz.state;

import java.util.HashMap;
import java.util.Map;

import com.humanharvest.organz.actions.ActionInvoker;

/**
 * A static class to store the current state of the system.
 */
public final class State {

    public enum DataStorageType {
        MEMORY, PUREDB
    }

    private static DataStorageType currentStorageType = DataStorageType.MEMORY;

    private static ClientManager clientManager;
    private static ClinicianManager clinicianManager;
    private static AdministratorManager administratorManager;
    private static AuthenticationManager authenticationManager;
    private static final Map<String, ActionInvoker> actionInvokers = new HashMap<>();

    private State() {
    }

    /**
     * Initialises a new action invoker, client manager and clinician manager.
     * Also binds an ActionOccurredListener to the new ActionInvoker
     */
    public static void init(DataStorageType storageType) {

        currentStorageType = storageType;

        authenticationManager = new AuthenticationManager();
        if (storageType == DataStorageType.PUREDB) {
            clientManager = new ClientManagerDBPure();
            clinicianManager = new ClinicianManagerDBPure();
            administratorManager = new AdministratorManagerDBPure();
        } else if (storageType == DataStorageType.MEMORY) {
            clientManager = new ClientManagerMemory();
            clinicianManager = new ClinicianManagerMemory();
            administratorManager = new AdministratorManagerMemory();
        } else {
            throw new IllegalArgumentException("DataStorageType cannot be null.");
        }
    }

    public static ClientManager getClientManager() {
        return clientManager;
    }

    public static ClinicianManager getClinicianManager() {
        return clinicianManager;
    }

    public static AdministratorManager getAdministratorManager() {
        return administratorManager;
    }

    public static AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public static void setAuthenticationManager(AuthenticationManager authenticationManager) {
        State.authenticationManager = authenticationManager;
    }

    public static void reset() {
        init(currentStorageType);
    }

    public static DataStorageType getCurrentStorageType() {
        return currentStorageType;
    }

    public static ActionInvoker getActionInvoker(String token) {
        ActionInvoker invoker = actionInvokers.get(token);
        if (invoker == null) {
            ActionInvoker newInvoker = new ActionInvoker();
            actionInvokers.put(token, newInvoker);
            return newInvoker;
        } else {
            return invoker;
        }
    }
}