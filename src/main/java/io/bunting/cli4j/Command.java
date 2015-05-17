package io.bunting.cli4j;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * TODO: Document this class
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
public @interface Command {
  String name() default "";
  String description() default "";
}
