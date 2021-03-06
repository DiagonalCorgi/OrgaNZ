package com.humanharvest.organz.resolvers.actions;

import com.humanharvest.organz.state.State;
import com.humanharvest.organz.views.ActionResponseView;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ActionResolverRest implements ActionResolver {

    private static HttpEntity<?> setupEntity() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.set("X-Auth-Token", State.getToken());

        return new HttpEntity<>(null, httpHeaders);
    }

    @Override
    public ActionResponseView executeUndo(String eTag) {
        HttpEntity<?> entity = setupEntity();

        ResponseEntity<ActionResponseView> responseEntity = State.getRestTemplate().exchange
                (State.getBaseUri() + "undo", HttpMethod.POST, entity, ActionResponseView.class);
        return responseEntity.getBody();
    }

    @Override
    public ActionResponseView executeRedo(String eTag) {
        HttpEntity<?> entity = setupEntity();

        ResponseEntity<ActionResponseView> responseEntity = State.getRestTemplate().exchange
                (State.getBaseUri() + "redo", HttpMethod.POST, entity, ActionResponseView.class);
        return responseEntity.getBody();
    }

    @Override
    public ActionResponseView getUndo() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.set("X-Auth-Token", State.getToken());

        HttpEntity<?> entity = new HttpEntity<>(null, httpHeaders);

        ResponseEntity<ActionResponseView> responseEntity = State.getRestTemplate().exchange
                (State.getBaseUri() + "undo", HttpMethod.GET, entity, ActionResponseView.class);
        return responseEntity.getBody();
    }

}