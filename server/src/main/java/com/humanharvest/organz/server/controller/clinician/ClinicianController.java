package com.humanharvest.organz.server.controller.clinician;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.humanharvest.organz.Clinician;
import com.humanharvest.organz.views.client.Views;
import com.humanharvest.organz.state.State;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClinicianController {

    /**
     * The GET /clinicians endpoint which returns all clinicians in the system.
     * @return all clinicians
     */
    @GetMapping("/clinicians")
    @JsonView(Views.Overview.class)
    public ResponseEntity<List<Clinician>> getClinicians() {
        return new ResponseEntity<>(State.getClinicianManager().getClinicians(), HttpStatus.OK);
    }

    /**
     * The GET /clinicians/{staffId} endpoint which returns the specified clinicians details
     * @param staffId the id of the clinician
     * @return the details of the specified clinician
     */
    @GetMapping("/clinicians/{staffId}")
    @JsonView(Views.Details.class)
    public ResponseEntity<Clinician> getCliniciansById(@PathVariable int staffId) {


        Clinician clinician = State.getClinicianManager().getClinicianByStaffId(staffId);
        if (clinician == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            HttpHeaders headers = new HttpHeaders();
//            headers.setETag(clinician.getEtag());
            return new ResponseEntity<>(clinician, headers, HttpStatus.OK);
        }
    }

}
