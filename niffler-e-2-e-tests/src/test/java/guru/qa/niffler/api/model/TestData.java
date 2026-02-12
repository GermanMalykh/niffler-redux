package guru.qa.niffler.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import guru.qa.niffler.common.jupiter.annotation.User;

public record TestData(
    @JsonIgnore String password,
    @JsonIgnore User.UserType userType
) {
}
