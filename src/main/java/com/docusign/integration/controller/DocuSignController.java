package com.docusign.integration.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.docusign.integration.dao.Receipient;
import com.docusign.integration.service.DocuSignService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/docusign")
public class DocuSignController {

    @Autowired
    DocuSignService docuSignService;

    @CrossOrigin
    @PostMapping(value = "/embeddedSending", consumes = {MediaType.APPLICATION_JSON_VALUE,
                                                            MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String,Object>> createEmbeddedSending(@RequestPart("receipient") String receipient,
                                                                    @RequestPart("file") List<MultipartFile> file) throws JsonMappingException, JsonProcessingException{
        List<Receipient> receipients = docuSignService.getJson(receipient);
        Map<String,Object> response = docuSignService.createEmbeddedSending(receipients, file);
        if(response == null) return new ResponseEntity<Map<String,Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<Map<String,Object>>(response, HttpStatus.CREATED);
    }

    @CrossOrigin
    @PostMapping(value = "/embeddedSignning", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,String>> createEmbeddedSignning(@Valid @RequestBody Receipient receipient, @RequestParam(value="envelopeId") String envelopeId){
        Map<String,String> response = docuSignService.createEmbeddedSignning(envelopeId, receipient);
        if(response == null) return new ResponseEntity<Map<String,String>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<Map<String,String>>(response, HttpStatus.CREATED);
    }
}
