package com.Wavey.WaveyService.domain.user.repository;

import com.Wavey.WaveyService.domain.user.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    // 제공처(Provider)와 고유 ID(ProviderId)를 모두 체크하여 유저 확인
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    @Query("select u.id from User u order by u.id")
    List<Long> findAllIds();

    @Query("select u.id from User u where u.id in :ids")
    List<Long> findExistingIds(@Param("ids") Collection<Long> ids);
}
