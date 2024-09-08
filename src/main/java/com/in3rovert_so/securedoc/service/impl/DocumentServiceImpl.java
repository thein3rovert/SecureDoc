package com.in3rovert_so.securedoc.service.impl;

import com.in3rovert_so.securedoc.dto.Document;
import com.in3rovert_so.securedoc.dto.api.IDocument;
import com.in3rovert_so.securedoc.entity.DocumentEntity;
import com.in3rovert_so.securedoc.exception.ApiException;
import com.in3rovert_so.securedoc.repository.DocumentRepository;
import com.in3rovert_so.securedoc.repository.UserRepository;
import com.in3rovert_so.securedoc.service.DocumentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.*;

import static com.in3rovert_so.securedoc.constant.Constants.FILE_STORAGE;
import static com.in3rovert_so.securedoc.utils.DocumentUtils.getDocumentUri;
import static com.in3rovert_so.securedoc.utils.DocumentUtils.setIcon;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.springframework.util.StringUtils.cleanPath;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Override
    public Page<IDocument> getDocuments(int page, int size) {
        return documentRepository.findDocument(PageRequest.of(page, size, Sort.by("name")));
    }

    @Override
    public Page<IDocument> getDocuments(int page, int size, String name) {
        return null;
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
                if("..".contains(filename)) {
                    throw new ApiException(String.format(("Invalid file name: %s", filename)));}
                    var documentEntity = DocumentEntity
                            .builder()
                            .documentId(UUID.randomUUID().toString())
                            .name(filename)
                            .owner(userEntity)
                            .extension(getExtension(filename))
                            .uri(getDocumentUri(filename))
                            .formattedSize(byteCountToDisplaySize(document.getSize()))
                            .icon(setIcon((getExtension(filename))))
                            .build();
            }
        }catch (Exception exception) {}
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
