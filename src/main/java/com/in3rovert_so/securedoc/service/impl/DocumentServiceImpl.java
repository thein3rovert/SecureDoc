package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.dto.api.IDocument;
import com.in3rovert_so.securedoc.repository.DocumentRepository;
import com.in3rovert_so.securedoc.service.DocumentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;

    @Override
    public Page<IDocument> getDocuments(int page, int size) {
        return documentRepository.findDocument(PageRequest.of(page, size, Sort.by("name")));
    }

    @Override
    public Page<IDocument> getDocuments(int page, int size, String name) {
        return null;
    }

    @Override
    public Collection<IDocument> saveDocuments(String userId, List<MultipartFile> documents) {
        return null;
    }

    @Override
    public IDocument updateDocument(String documentId, String name, String description) {
        return null;
    }

    @Override
    public IDocument getDocumentByDocumentId(String documentId) {
        return null;
    }

    @Override
    public void deleteDocument(String documentId) {

    }

    @Override
    public Resource getResource(String documentName) {
        return null;
    }
}
