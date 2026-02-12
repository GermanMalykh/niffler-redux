package guru.qa.niffler.common.jupiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface User {

  UserType value() default UserType.PENDING;

  enum UserType {
    WITH_FRIENDS, PENDING, INCOMING_INVITE
  }
}
