package org.springframework.annotation.aop;

import java.lang.annotation.*;

/**
 * @author Reece
 * @ClassName Aspect.java
 * @Description TODO
 * @createTime 2021年05月10日 10:28:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Aspect {
}
