package com.in3rovert_so.securedoc.resource;

import com.in3rovert_so.securedoc.domain.Response;
import com.in3rovert_so.securedoc.dto.User;
import com.in3rovert_so.securedoc.dtorequest.updateDocumentRequest;
import com.in3rovert_so.securedoc.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.in3rovert_so.securedoc.utils.RequestUtils.getResponse;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

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
    @GetMapping
    public ResponseEntity<Response> getDocuments(@AuthenticationPrincipal User user, HttpServletRequest request,
                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                @RequestParam(value = "size", defaultValue = "5") int size) {
        var documents = documentService.getDocuments(page, size);
        //return ResponseEntity.ok().body(getResponse(request, Map.of("documents", documents), "Document's uploaded", OK));
        return ResponseEntity.ok().body(getResponse(request, Map.of("documents", documents), "Document's Retrieved", OK));
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchDocuments(@AuthenticationPrincipal User user, HttpServletRequest request,
                                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "size", defaultValue = "5") int size,
                                                 @RequestParam(value = "name", defaultValue = "") String name) {
        var documents = documentService.getDocuments(page, size, name);
        //return ResponseEntity.ok().body(getResponse(request, Map.of("documents", documents), "Document's uploaded", OK));
        return ResponseEntity.ok().body(getResponse(request, Map.of("documents", documents), "Document's Retrieved", OK));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<Response> getDocument(@AuthenticationPrincipal User user, @PathVariable("documentId") String documentId, HttpServletRequest request){
        var document = documentService.getDocumentByDocumentId(documentId);
        return ResponseEntity.ok().body(getResponse(request, Map.of("document", document), "Document's Retrieved", OK));
    }

    @PatchMapping
    public ResponseEntity<Response> UpdateDocument(@AuthenticationPrincipal User user, @RequestBody updateDocumentRequest document, HttpServletRequest request){
        var updatedDocument = documentService.updateDocument(document.getDocumentId(), document.getName(), document.getDescription());
        return ResponseEntity.ok().body(getResponse(request, Map.of("updatedDocument", updatedDocument), "Document's Updated", OK));
    }





/*    protected URI getUri() {
        return URI.create("");

    }*/
}
