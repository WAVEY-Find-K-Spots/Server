package com.Wavey.WaveyService.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryConstraintTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 서로_다른_공급자는_동일한_providerId를_사용할_수_있다() {
        userRepository.saveAndFlush(user("google", "same-provider-id", "google@example.com"));
        userRepository.saveAndFlush(user("kakao", "same-provider-id", "kakao@example.com"));

        assertThat(userRepository.findAll()).hasSize(2);
    }

    @Test
    void 같은_공급자의_providerId_중복은_허용하지_않는다() {
        userRepository.saveAndFlush(user("kakao", "duplicate-id", "first@example.com"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(user("kakao", "duplicate-id", "second@example.com"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    private User user(String provider, String providerId, String email) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .name("테스트 사용자")
                .role(Role.USER)
                .build();
    }
}
