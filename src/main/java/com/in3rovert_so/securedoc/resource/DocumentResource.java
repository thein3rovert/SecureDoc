package com.in3rovert_so.securedoc.resource;

import com.in3rovert_so.securedoc.domain.Response;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.in3rovert_so.securedoc.utils.RequestUtils.getResponse;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/documents"})
public class DocumentResource {
    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<Response> saveDocuments(@AuthenticationPrincipal User user, @RequestParam("files") List<MultipartFile> documents, HttpServletRequest request) {
        var newDocuments = documentService.saveDocuments(user.getUserId(), documents);
        return ResponseEntity.created(URI.create("")).body(getResponse(request, Map.of("documents", newDocuments), "Document's uploaded",CREATED));
    }

/*    protected URI getUri() {
        return URI.create("");

    }*/
}
