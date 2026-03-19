package com.Wavey.WaveyService.domain.user.repository;

import com.Wavey.WaveyService.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이미 가입된 유저인지 ProviderId로 확인
    Optional<User> findByProviderId(String providerId);
}