package com.Wavey.WaveyService.domain.user.repository;

import com.Wavey.WaveyService.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 제공처(Provider)와 고유 ID(ProviderId)를 모두 체크하여 유저 확인
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}