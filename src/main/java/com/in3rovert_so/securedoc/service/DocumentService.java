package com.in3rovert_so.securedoc.service;

import com.in3rovert_so.securedoc.dto.api.IDocument;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface DocumentService {

    // RETURNING A PAGE
    Page<IDocument> getDocuments(int page, int size); // For getting the document
    Page<IDocument> getDocuments(int page, int size, String name); // For searching the document.
    Collection<IDocument>saveDocuments(String userId, List<MultipartFile> documents); // For all the document we want to save.


    // Returning a document object
    IDocument updateDocument(String documentId, String name, String description);
    IDocument getDocumentByDocumentId(String documentId);
    void deleteDocument(String documentId);


    Resource getResource(String documentName);
}
