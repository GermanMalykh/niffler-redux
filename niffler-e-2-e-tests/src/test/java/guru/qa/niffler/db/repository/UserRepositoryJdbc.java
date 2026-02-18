package guru.qa.niffler.db.repository;

import guru.qa.niffler.db.DataSourceProvider;
import guru.qa.niffler.db.JdbcUrl;
import guru.qa.niffler.db.model.Authority;
import guru.qa.niffler.db.model.AuthorityEntity;
import guru.qa.niffler.db.model.CurrencyValues;
import guru.qa.niffler.db.model.UserAuthEntity;
import guru.qa.niffler.db.model.UserEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class UserRepositoryJdbc implements UserRepository {

    private final DataSource authDs = DataSourceProvider.INSTANCE.dataSource(JdbcUrl.AUTH);
    private final DataSource udDs = DataSourceProvider.INSTANCE.dataSource(JdbcUrl.USERDATA);

    private final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    public UserAuthEntity createInAuth(UserAuthEntity user) {
        try (Connection conn = authDs.getConnection()) {
            conn.setAutoCommit(false);
            UUID authUserId;
            try {
                try (PreparedStatement userPs = conn.prepareStatement(
                        "INSERT INTO \"user\" " +
                                "(username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                                "VALUES (?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                     PreparedStatement authorityPs = conn.prepareStatement(
                             "INSERT INTO \"authority\" " +
                                     "(user_id, authority) " +
                                     "VALUES (?, ?)")
                ) {
                    userPs.setString(1, user.getUsername());
                    userPs.setString(2, pe.encode(user.getPassword()));
                    userPs.setBoolean(3, user.getEnabled());
                    userPs.setBoolean(4, user.getAccountNonExpired());
                    userPs.setBoolean(5, user.getAccountNonLocked());
                    userPs.setBoolean(6, user.getCredentialsNonExpired());

                    userPs.executeUpdate();

                    try (ResultSet keys = userPs.getGeneratedKeys()) {
                        if (keys.next()) {
                            authUserId = UUID.fromString(keys.getString("id"));
                        } else {
                            throw new IllegalStateException("Can`t find id");
                        }
                    }

                    for (Authority authority : Authority.values()) {
                        authorityPs.setObject(1, authUserId);
                        authorityPs.setString(2, authority.name());
                        authorityPs.addBatch();
                        authorityPs.clearParameters();
                    }

                    authorityPs.executeBatch();
                }
                conn.commit();
                user.setId(authUserId);
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @Override
    public UserEntity createInUserdata(UserEntity user) {
        try (Connection conn = udDs.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO \"user\" " +
                                "(username, currency) " +
                                "VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getCurrency().name());
                    ps.executeUpdate();

                    UUID userId;
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            userId = UUID.fromString(keys.getString("id"));
                        } else {
                            throw new IllegalStateException("Can`t find id");
                        }
                    }
                    user.setId(userId);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @Override
    public Optional<UserAuthEntity> findByIdInAuth(UUID id) {
        UserAuthEntity userAuth = new UserAuthEntity();
        try (Connection conn = authDs.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement userPs = conn.prepareStatement(
                        "SELECT * FROM \"user\" WHERE id = ?")) {
                    userPs.setObject(1, id);
                    ResultSet rs = userPs.executeQuery();
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    userAuth.setId(rs.getObject("id", UUID.class));
                    userAuth.setUsername(rs.getString("username"));
                    userAuth.setPassword(rs.getString("password"));
                    userAuth.setEnabled(rs.getBoolean("enabled"));
                    userAuth.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    userAuth.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    userAuth.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                }
                try (PreparedStatement authorityPs = conn.prepareStatement(
                        "SELECT id, authority FROM \"authority\" WHERE user_id = ?")) {
                    authorityPs.setObject(1, userAuth.getId());
                    try (ResultSet authRs = authorityPs.executeQuery()) {
                        while (authRs.next()) {
                            AuthorityEntity ae = new AuthorityEntity();
                            ae.setId(authRs.getObject("id", UUID.class));
                            ae.setAuthority(Authority.valueOf(authRs.getString("authority")));
                            userAuth.getAuthorities().add(ae);
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.of(userAuth);
    }

    @Override
    public Optional<UserEntity> findByIdInUserdata(UUID id) {
        UserEntity user = new UserEntity();
        try (Connection conn = udDs.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement userPs = conn.prepareStatement(
                        "SELECT * FROM \"user\" WHERE id = ?")) {
                    userPs.setObject(1, id);
                    ResultSet rs = userPs.executeQuery();
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    user.setId(rs.getObject("id", UUID.class));
                    user.setUsername(rs.getString("username"));
                    user.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
                    user.setFirstname(rs.getString("firstname"));
                    user.setSurname(rs.getString("surname"));
                    user.setPhoto(rs.getBytes("photo"));
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.of(user);
    }

    @Override
    public UserAuthEntity updateUserInAuth(UserAuthEntity user) {
        try (Connection conn = authDs.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement userPs = conn.prepareStatement(
                        "UPDATE \"user\" SET username=?, password=?, enabled=?," +
                                "account_non_expired=?, account_non_locked=?, credentials_non_expired=? WHERE id=?");
                     PreparedStatement authorityDelPs = conn.prepareStatement(
                             "DELETE FROM \"authority\" WHERE user_id=?")) {
                    userPs.setString(1, user.getUsername());
                    userPs.setString(2, pe.encode(user.getPassword()));
                    userPs.setBoolean(3, user.getEnabled());
                    userPs.setBoolean(4, user.getAccountNonExpired());
                    userPs.setBoolean(5, user.getAccountNonLocked());
                    userPs.setBoolean(6, user.getCredentialsNonExpired());
                    userPs.setObject(7, user.getId());
                    userPs.executeUpdate();

                    authorityDelPs.setObject(1, user.getId());
                    authorityDelPs.executeUpdate();
                }
                try (PreparedStatement authorityInsPs = conn.prepareStatement(
                        "INSERT INTO \"authority\" (user_id, authority) VALUES (?, ?)")) {
                    if (user.getAuthorities() != null) {
                        for (AuthorityEntity ae : user.getAuthorities()) {
                            authorityInsPs.setObject(1, user.getId());
                            authorityInsPs.setString(2, ae.getAuthority().name());
                            authorityInsPs.addBatch();
                            authorityInsPs.clearParameters();
                        }
                        authorityInsPs.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @Override
    public UserEntity updateUserInUserdata(UserEntity user) {
        try (Connection conn = udDs.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement userPs = conn.prepareStatement(
                        "UPDATE \"user\" SET " +
                                "username = ?, currency = ?, firstname = ?, surname = ?, photo = ? WHERE id = ?")) {
                    userPs.setString(1, user.getUsername());
                    userPs.setString(2, user.getCurrency().name());
                    userPs.setString(3, user.getFirstname());
                    userPs.setString(4, user.getSurname());
                    userPs.setObject(5, user.getPhoto());
                    userPs.setObject(6, user.getId());

                    userPs.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @Override
    public void deleteInAuthById(UUID id) {
        try (Connection conn = authDs.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement authorityPs = conn.prepareStatement(
                        "DELETE FROM \"authority\" WHERE user_id = ?");
                     PreparedStatement userPs = conn.prepareStatement(
                             "DELETE FROM \"user\" WHERE id = ?")) {

                    authorityPs.setObject(1, id);
                    authorityPs.executeUpdate();

                    userPs.setObject(1, id);
                    userPs.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteInUserdataById(UUID id) {
        try (Connection conn = udDs.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement friendshipPs = conn.prepareStatement(
                        "DELETE FROM \"friendship\" WHERE user_id = ? OR friend_id = ?");
                     PreparedStatement userPs = conn.prepareStatement(
                             "DELETE FROM \"user\" WHERE id = ?")) {

                    friendshipPs.setObject(1, id);
                    friendshipPs.setObject(2, id);
                    friendshipPs.executeUpdate();

                    userPs.setObject(1, id);
                    userPs.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
