package com.in3rovert_so.securedoc.repository;

import com.in3rovert_so.securedoc.entity.ConfirmationEntity;
import com.in3rovert_so.securedoc.entity.UserEntity;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ConfirmationRepository extends JpaRepository<ConfirmationEntity, Long> {
    Optional<ConfirmationEntity> findByKey(String key);

    Optional<ConfirmationEntity>findByUserEntity(UserEntity userEntity);
}
