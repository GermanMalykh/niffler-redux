package guru.qa.niffler.common.jupiter.extension;

import guru.qa.niffler.common.jupiter.annotation.DbUser;
import guru.qa.niffler.db.model.Authority;
import guru.qa.niffler.db.model.AuthorityEntity;
import guru.qa.niffler.db.model.CurrencyValues;
import guru.qa.niffler.db.model.UserAuthEntity;
import guru.qa.niffler.db.model.UserEntity;
import guru.qa.niffler.db.repository.UserRepository;
import guru.qa.niffler.db.repository.UserRepositoryJdbc;
import guru.qa.niffler.db.repository.UserRepositorySJdbc;
import net.datafaker.Faker;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Arrays;

public class DbUserExtension implements Extension, BeforeEachCallback, AfterTestExecutionCallback, ParameterResolver {
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DbUserExtension.class);
    private String repository = System.getProperty("repository", "jdbc");
    private UserRepository userRepository = "sjdbc".equals(repository)
            ? new UserRepositorySJdbc()
            : new UserRepositoryJdbc();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        DbUser dbUser = AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                DbUser.class
        ).orElse(
                AnnotationSupport.findAnnotation(
                        context.getRequiredTestClass(),
                        DbUser.class
                ).orElse(null)
        );

        if (dbUser != null) {
            Faker faker = new Faker();
            String username = (dbUser.username() == null || dbUser.username().isBlank())
                    ? faker.credentials().username()
                    : dbUser.username();

            String password = (dbUser.password() == null || dbUser.password().isBlank())
                    ? faker.credentials().password()
                    : dbUser.password();


            UserAuthEntity userAuth = new UserAuthEntity();
            userAuth.setUsername(username);
            userAuth.setPassword(password);
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
            userRepository.createInAuth(userAuth);
            context.getStore(NAMESPACE).put("userAuth", userAuth);

            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setCurrency(CurrencyValues.RUB);
            userRepository.createInUserdata(user);
            context.getStore(NAMESPACE).put("user", user);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(UserAuthEntity.class)
                || parameterContext.getParameter().getType().equals(UserEntity.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        Object value = type.equals(UserAuthEntity.class)
                ? extensionContext.getStore(NAMESPACE).get("userAuth", UserAuthEntity.class)
                : extensionContext.getStore(NAMESPACE).get("user", UserEntity.class);
        if (value == null) {
            throw new ParameterResolutionException(
                    "No user in store. Add @DbUser on the test method or class.");
        }
        return value;
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        UserAuthEntity userAuth = extensionContext.getStore(NAMESPACE).get("userAuth", UserAuthEntity.class);
        UserEntity user = extensionContext.getStore(NAMESPACE).get("user", UserEntity.class);
        if (userAuth != null && user != null) {
            userRepository.deleteInAuthById(userAuth.getId());
            userRepository.deleteInUserdataById(user.getId());
        }
    }
}
