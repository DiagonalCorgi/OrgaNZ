package com.humanharvest.organz.server.controller;

import java.util.Collection;
import java.util.Map;

import com.humanharvest.organz.DonatedOrgan;
import com.humanharvest.organz.server.exceptions.GlobalControllerExceptionHandler;
import com.humanharvest.organz.state.State;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrgansController {

    /**
     * The GET endpoint for getting all organs currently available to be donated
     * @param authToken authentication token - only clinicians and administrators can access donatable organs
     * @return response entity containing all organs that are available for donation
     * @throws GlobalControllerExceptionHandler.InvalidRequestException
     */
    @GetMapping("/organs")
    public ResponseEntity<Collection<DonatedOrgan>> getOrgansToDonate(
            @RequestHeader(value = "X-Auth-Token", required = false) String authToken)
            throws GlobalControllerExceptionHandler.InvalidRequestException {

        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);
        System.out.println(authToken);

        Collection<DonatedOrgan> donatedOrgans = State.getClientManager().getAllOrgansToDonate();
        return new ResponseEntity<>(donatedOrgans, HttpStatus.OK);
    }

    @DeleteMapping("/organs")
    public ResponseEntity<DonatedOrgan> manuallyExpireOrgan(
            @RequestBody DonatedOrgan organ,
            @RequestHeader(value = "X-Auth-Token",required = false) String authToken)
            throws GlobalControllerExceptionHandler.InvalidRequestException {
        State.getAuthenticationManager().verifyClinicianOrAdmin(authToken);

        State.getClientManager().getAllOrgansToDonate().remove(organ);

        return new ResponseEntity<>(organ,HttpStatus.OK);

    }

}
