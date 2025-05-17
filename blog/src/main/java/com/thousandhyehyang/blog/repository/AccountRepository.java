package com.thousandhyehyang.blog.repository;

import com.thousandhyehyang.blog.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Account> findByProviderAndEmail(String provider, String email);

    Optional<Account> findByNickname(String nickname);

    boolean existsByNickname(String nickname);
}
