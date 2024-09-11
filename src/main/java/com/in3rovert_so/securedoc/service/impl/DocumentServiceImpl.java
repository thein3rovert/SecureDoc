package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.dto.Document;
import com.in3rovert_so.securedoc.dto.api.IDocument;
import com.in3rovert_so.securedoc.entity.DocumentEntity;
import com.in3rovert_so.securedoc.exception.ApiException;
import com.in3rovert_so.securedoc.repository.DocumentRepository;
import com.in3rovert_so.securedoc.repository.UserRepository;
import com.in3rovert_so.securedoc.service.DocumentService;
import com.in3rovert_so.securedoc.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.in3rovert_so.securedoc.constant.Constants.FILE_STORAGE;
import static com.in3rovert_so.securedoc.utils.DocumentUtils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.springframework.util.StringUtils.cleanPath;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public Page<IDocument> getDocuments(int page, int size) {
        return documentRepository.findDocument(PageRequest.of(page, size, Sort.by("name")));
    }

    @Override
    public Page<IDocument> getDocuments(int page, int size, String name) {
        // When searched: Sort, paginate and search by name
        return documentRepository.findDocumentByName(name, PageRequest.of(page, size, Sort.by("name")));
    }

    @Override
    public Collection<Document> saveDocuments(String userId, List<MultipartFile> documents) {
        List<Document> newDocuments = new ArrayList<>();
        var userEntity = userRepository.findUserByUserId(userId).get();
        var storage = Paths.get(FILE_STORAGE).toAbsolutePath().normalize();
        // Loop through all the files then save
        try {
            // For every document in thr documents
            for(MultipartFile document : documents) {
                var filename = cleanPath(Objects.requireNonNull(document.getOriginalFilename()));
                System.out.print(filename);
                if("..".contains(filename)) {
                    throw new ApiException(String.format("Invalid file name: %s", filename));}
                    var documentEntity = DocumentEntity
                            .builder()
                            .documentId(UUID.randomUUID().toString())
                            .name(filename)
                            .owner(userEntity)
                            .extension(getExtension(filename))
                            .uri(getDocumentUri(filename))
                            .size(document.getSize())
                            .formattedSize(byteCountToDisplaySize(document.getSize()))
                            .icon(setIcon((getExtension(filename))))
                            .build();
                System.out.print(documentEntity);
                var savedDocument = documentRepository.save(documentEntity);
                System.out.println("Document to be saved " + savedDocument);
                // Lets save the physical file on the computer.
                Files.copy(document.getInputStream(), storage.resolve(filename), REPLACE_EXISTING);
                // Lets generate a document
                Document newDocument = fromDocumentEntity(savedDocument, userService.getUserById(savedDocument.getCreatedBy()), userService.getUserById(savedDocument.getUpdatedBy()));
                System.out.print("Document to be added: " + newDocument);
                newDocuments.add(newDocument);
                System.out.println(newDocument);
            }
            return newDocuments;
        }catch (Exception exception) {
            throw new ApiException("Unable to save documents.");
        }
    }

    @Override
    public IDocument updateDocument(String documentId, String name, String description) {
        return null;
    }

    @Override
    public IDocument getDocumentByDocumentId(String documentId) {
        return documentRepository.findDocumentByDocumentId(documentId).orElseThrow(() -> new ApiException("Document not found"));
    }

    @Override
    public void deleteDocument(String documentId) {

    }

    @Override
    public Resource getResource(String documentName) {
        return null;
    }
}
