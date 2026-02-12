package guru.qa.niffler.common.jupiter.extension;

import guru.qa.niffler.api.model.CurrencyValues;
import guru.qa.niffler.api.model.TestData;
import guru.qa.niffler.api.model.UserJson;

import guru.qa.niffler.common.jupiter.annotation.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static guru.qa.niffler.common.jupiter.annotation.User.UserType.*;

public class UsersQueueExtension implements BeforeEachCallback, AfterTestExecutionCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE
            = ExtensionContext.Namespace.create(UsersQueueExtension.class);

    private static Map<User.UserType, Queue<UserJson>> userQueues = new ConcurrentHashMap<>();

    static {
        Queue<UserJson> friendsQueue = new ConcurrentLinkedQueue<>();
        Queue<UserJson> pendingInviteQueue = new ConcurrentLinkedQueue<>();
        Queue<UserJson> incomingInviteQueue = new ConcurrentLinkedQueue<>();

        friendsQueue.add(user("Kafka", "12345", WITH_FRIENDS));
        friendsQueue.add(user("Pizzly", "12345", WITH_FRIENDS));
        userQueues.put(WITH_FRIENDS, friendsQueue);

        incomingInviteQueue.add(user("Boris", "12345", INCOMING_INVITE));
        incomingInviteQueue.add(user("Valentin", "12345", INCOMING_INVITE));
        userQueues.put(INCOMING_INVITE, incomingInviteQueue);

        pendingInviteQueue.add(user("Matt", "12345", PENDING));
        pendingInviteQueue.add(user("Margo", "12345", PENDING));
        userQueues.put(PENDING, pendingInviteQueue);

    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        List<Parameter> allUserParams = new ArrayList<>();
        Method testMethod = context.getRequiredTestMethod();
        Method beforeEachMethod = null;

        for (Method m : context.getRequiredTestClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(BeforeEach.class)) {
                beforeEachMethod = m;
                break;
            }
        }
        if (beforeEachMethod != null) {
            for (Parameter p : beforeEachMethod.getParameters()) {
                if (p.isAnnotationPresent(User.class) && p.getType().isAssignableFrom(UserJson.class)) {
                    allUserParams.add(p);
                }
            }
        }
        for (Parameter p : testMethod.getParameters()) {
            if (p.isAnnotationPresent(User.class) && p.getType().isAssignableFrom(UserJson.class)) {
                allUserParams.add(p);
            }
        }

        Map<String, UserJson> usersForTest = new LinkedHashMap<>();
        for (Parameter parameter : allUserParams) {
            User annotation = parameter.getAnnotation(User.class);
            if (annotation != null && parameter.getType().isAssignableFrom(UserJson.class)) {
                Queue<UserJson> queue = userQueues.get(annotation.value());
                UserJson testCandidate = queue.poll();
                if (testCandidate == null) {
                    throw new ParameterResolutionException("No users available for type: " + annotation.value());
                }
                int paramIndex = Arrays.asList(parameter.getDeclaringExecutable().getParameters()).indexOf(parameter);
                String key = parameter.getDeclaringExecutable().getName() + ":" + paramIndex;
                usersForTest.put(key, testCandidate);
            }
        }

        context.getStore(NAMESPACE).put(context.getUniqueId(), usersForTest);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, UserJson> usersForTest = (Map<String, UserJson>) context.getStore(NAMESPACE)
                .get(context.getUniqueId());
        if (usersForTest != null) {
            for (UserJson u : usersForTest.values()) {
                userQueues.get(u.testData().userType()).add(u);
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter()
                .getType()
                .isAssignableFrom(UserJson.class) &&
                parameterContext.getParameter().isAnnotationPresent(User.class);
    }

    @Override
    public UserJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Parameter param = parameterContext.getParameter();
        int paramIndex = Arrays.asList(param.getDeclaringExecutable().getParameters()).indexOf(param);
        String key = param.getDeclaringExecutable().getName() + ":" + paramIndex;

        @SuppressWarnings("unchecked")
        Map<String, UserJson> usersForTest = (Map<String, UserJson>) extensionContext.getStore(NAMESPACE)
                .get(extensionContext.getUniqueId());
        if (usersForTest == null) {
            throw new ParameterResolutionException("Users not found in store for key: " + key);
        }
        UserJson user = usersForTest.get(key);
        if (user == null) {
            throw new ParameterResolutionException("User not found for parameter key: " + key);
        }
        return user;
    }

    private static UserJson user(String username, String password, User.UserType userType) {
        return new UserJson(
                null,
                username,
                null,
                null,
                CurrencyValues.RUB,
                null,
                null,
                new TestData(
                        password,
                        userType
                )
        );
    }
}
