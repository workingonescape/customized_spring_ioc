package org.springframework.annotation;

import java.lang.annotation.*;

/**
 * @author Reece
 * @ClassName Repository.java
 * @Description TODO
 * @createTime 2021年05月09日 12:49:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Repository {
    String value() default "";
}
