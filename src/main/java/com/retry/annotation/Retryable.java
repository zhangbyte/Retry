package com.retry.annotation;

import java.lang.annotation.*;

/**
 * Created by zbyte on 17-7-19.
 *
 * 使用在接口方法处，客户端服务端都针对该接口方法进行拦截，并织入retry逻辑
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {
}
