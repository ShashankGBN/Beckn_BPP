package com.tibil.BecknBPP.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibil.BecknBPP.controller.api.ConfirmApi;
import com.tibil.BecknBPP.dto.ConfirmBody;
import com.tibil.BecknBPP.dto.InlineResponse200;
import com.tibil.BecknBPP.service.ConfirmService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2023-03-07T06:09:48.574791157Z[GMT]")
@RestController
public class ConfirmApiController implements ConfirmApi {

    private static final Logger log = LoggerFactory.getLogger(ConfirmApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    private final ConfirmService confirmService;

    @org.springframework.beans.factory.annotation.Autowired
    public ConfirmApiController(ObjectMapper objectMapper, HttpServletRequest request, ConfirmService confirmService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.confirmService = confirmService;
    }

    public ResponseEntity<InlineResponse200> confirmPost(@Parameter(in = ParameterIn.DEFAULT, description = "TODO", schema=@Schema()) @Valid @RequestBody ConfirmBody body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
            	log.info(body.toString());
            	confirmService.processInternalRequest(body);
                return new ResponseEntity<InlineResponse200>(objectMapper.readValue("{\n  \"message\" : {\n    \"ack\" : {\n      \"status\" : \"ACK\"\n    }\n  }\n}", InlineResponse200.class), HttpStatus.OK);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<InlineResponse200>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<InlineResponse200>(HttpStatus.NOT_IMPLEMENTED);
    }

}
