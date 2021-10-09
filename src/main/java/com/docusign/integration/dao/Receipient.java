package com.docusign.integration.dao;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Receipient {
    
    @NotEmpty
    @NotNull
    private String name;
    @NotEmpty
    @NotNull
    private String emailId;
}
