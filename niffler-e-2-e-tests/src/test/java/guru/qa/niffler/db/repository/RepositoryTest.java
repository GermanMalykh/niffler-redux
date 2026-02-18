package guru.qa.niffler.db.repository;

import guru.qa.niffler.common.jupiter.extension.UserRepositoryExtension;
import guru.qa.niffler.db.model.Authority;
import guru.qa.niffler.db.model.AuthorityEntity;
import guru.qa.niffler.db.model.CurrencyValues;
import guru.qa.niffler.db.model.UserAuthEntity;
import guru.qa.niffler.db.model.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UserRepositoryExtension.class)
public class RepositoryTest {
    private UserRepository userRepository;
    private UserAuthEntity userAuth;
    private UserEntity user;

    @BeforeEach
    void createUser() {
        String uniqueUsername = "valentin_" + UUID.randomUUID().toString().substring(0, 8);
        userAuth = new UserAuthEntity();
        userAuth.setUsername(uniqueUsername);
        userAuth.setPassword("12345");
        userAuth.setEnabled(true);
        userAuth.setAccountNonExpired(true);
        userAuth.setAccountNonLocked(true);
        userAuth.setCredentialsNonExpired(true);
        userAuth.setAuthorities(Arrays.stream(Authority.values())
                .map(e -> {
                    AuthorityEntity ae = new AuthorityEntity();
                    ae.setAuthority(e);
                    return ae;
                }).toList()
        );

        user = new UserEntity();
        user.setUsername(uniqueUsername);
        user.setCurrency(CurrencyValues.RUB);
        userRepository.createInAuth(userAuth);
        userRepository.createInUserdata(user);
    }

    @AfterEach
    void removeUser() {
        userRepository.deleteInAuthById(userAuth.getId());
        userRepository.deleteInUserdataById(user.getId());
    }

    @Test
    @DisplayName("findByIdInAuth возвращает созданного пользователя")
    void findByIdInAuthReturnsCreatedUser() {
        Optional<UserAuthEntity> found = userRepository.findByIdInAuth(userAuth.getId());
        assertTrue(found.isPresent());
        assertEquals(userAuth.getId(), found.get().getId());
        assertEquals(userAuth.getUsername(), found.get().getUsername());
        assertTrue(found.get().getEnabled());
        assertEquals(2, found.get().getAuthorities().size());
    }

    @Test
    @DisplayName("findByIdInUserdata возвращает созданного пользователя")
    void findByIdInUserdataReturnsCreatedUser() {
        Optional<UserEntity> found = userRepository.findByIdInUserdata(user.getId());
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
        assertEquals(user.getUsername(), found.get().getUsername());
        assertEquals(CurrencyValues.RUB, found.get().getCurrency());
    }

    @Test
    @DisplayName("updateUserInAuth обновляет пользователя в auth и возвращает сущность")
    void updateUserInAuthUpdatesAndReturnsEntity() {
        userAuth.setUsername("valentin_updated");
        userAuth.setPassword("newpass");
        userAuth.setEnabled(false);

        UserAuthEntity updated = userRepository.updateUserInAuth(userAuth);
        assertNotNull(updated);
        assertEquals(userAuth.getId(), updated.getId());
        assertEquals("valentin_updated", updated.getUsername());

        Optional<UserAuthEntity> found = userRepository.findByIdInAuth(userAuth.getId());
        assertTrue(found.isPresent());
        assertEquals("valentin_updated", found.get().getUsername());
        assertFalse(found.get().getEnabled());
    }

    @Test
    @DisplayName("updateUserInUserdata обновляет пользователя в userdata и возвращает сущность")
    void updateUserInUserdataUpdatesAndReturnsEntity() {
        user.setUsername("valentin_updated");
        user.setCurrency(CurrencyValues.USD);
        user.setFirstname("Valentin");
        user.setSurname("Updated");

        UserEntity updated = userRepository.updateUserInUserdata(user);
        assertNotNull(updated);
        assertEquals(user.getId(), updated.getId());
        assertEquals("valentin_updated", updated.getUsername());
        assertEquals(CurrencyValues.USD, updated.getCurrency());
        assertEquals("Valentin", updated.getFirstname());
        assertEquals("Updated", updated.getSurname());

        Optional<UserEntity> found = userRepository.findByIdInUserdata(user.getId());
        assertTrue(found.isPresent());
        assertEquals("valentin_updated", found.get().getUsername());
        assertEquals(CurrencyValues.USD, found.get().getCurrency());
        assertEquals("Valentin", found.get().getFirstname());
        assertEquals("Updated", found.get().getSurname());
    }
}
