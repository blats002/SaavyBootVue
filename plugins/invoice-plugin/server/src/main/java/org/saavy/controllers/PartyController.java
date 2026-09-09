package org.saavy.controllers;

import org.saavy.entity.Party;
import org.saavy.entity.PartyDTO;
import org.saavy.services.JPAService;
import org.saavy.services.PartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parties")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class PartyController extends BaseController<Party, PartyDTO, Long> {

    @Autowired
    private PartyService partyService;

    @Override
    protected JPAService<Party, PartyDTO, Long> getService() {
        return partyService;
    }
}
