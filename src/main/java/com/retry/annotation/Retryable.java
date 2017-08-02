package com.retry.annotation;

import java.lang.annotation.*;

/**
 * Created by zbyte on 17-7-19.
 *
 * 客户端：使用在接口方法处，对kepler代理对应方法进行拦截，并织入retry逻辑
 *       使用在实现类方法上，对该方法拦截织入retry逻辑
 *
 * 服务端：使用在接口方法处，对实现类对应方法进行拦截并织入幂等处理逻辑
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {
}
