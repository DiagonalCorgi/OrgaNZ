package com.humanharvest.organz.state;

import com.humanharvest.organz.Client;

/**
 * Fake stub for authentication. DO NOT USE IN PRODUCTION.
 */
public class AuthenticationManagerFake extends AuthenticationManager {

    @Override
    protected boolean checkClient(String identifier, Client viewedClient) {
        return true;
    }

    @Override
    protected boolean checkClinician(String identifier) {
        return true;
    }

    @Override
    protected boolean checkAdmin(String identifier) {
        return true;
    }

    @Override
    protected String getIdentifierFromToken(String token) {
        return "EMPTY";
    }
}
