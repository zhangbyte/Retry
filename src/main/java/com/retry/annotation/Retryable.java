package com.retry.annotation;

import java.lang.annotation.*;

/**
 * Created by zbyte on 17-7-19.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {
}
