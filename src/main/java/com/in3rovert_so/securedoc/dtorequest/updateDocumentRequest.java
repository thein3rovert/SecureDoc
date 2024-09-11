package com.in3rovert_so.securedoc.dtorequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class updateDocumentRequest {
    @NotEmpty(message = "DocumentId cannot be empty or null")
    private String documentId;
    @NotEmpty(message = "Document Name cannot be empty or null")
    private String name;
    @NotEmpty(message = "Document Description cannot be empty or null")
    private String description;
}
