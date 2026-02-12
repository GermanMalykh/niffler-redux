package guru.qa.niffler.common.jupiter.annotation;

import guru.qa.niffler.common.jupiter.extension.AllureIdConverter;
import org.junit.jupiter.params.converter.ConvertWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ConvertWith(AllureIdConverter.class)
public @interface AllureIdParam {
}
