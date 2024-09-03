package com.in3rovert_so.securedoc.repository;

import com.in3rovert_so.securedoc.constant.Constants;
import com.in3rovert_so.securedoc.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.print.Doc;

import static com.in3rovert_so.securedoc.constant.Constants.SELECT_COUNT_DOCUMENT_QUERY;
import static com.in3rovert_so.securedoc.constant.Constants.SELECT_DOCUMENT_QUERY;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    /*
    Working with sql with Jpa at the same time
     */

    @Query(countQuery = SELECT_COUNT_DOCUMENT_QUERY, value = SELECT_DOCUMENT_QUERY, nativeQuery = true)
    Page<DocumentEntity> findDocument(Pageable pageable);
}
