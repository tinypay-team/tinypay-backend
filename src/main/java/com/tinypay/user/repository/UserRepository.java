package com.tinypay.user.repository;

import com.tinypay.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
public interface UserRepository extends JpaRepository<User, Long> {
}
=======
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderId(String providerId);
}
>>>>>>> 3a588cf (feat: 구글 소셜 로그인 구현)
