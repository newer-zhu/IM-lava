package com.github.yuanrw.im.rest.web.filter;

import com.github.yuanrw.im.common.util.IdWorker;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Service
public class TokenManager {

    //value is userId
    private static final String SESSION_KEY = "IM:TOKEN:";
    private ReactiveRedisTemplate<String, String> template;

    public TokenManager(ReactiveRedisTemplate<String, String> template) {
        this.template = template;
    }

    /**
     * @author hodor_zhu
     * @description  refresh token and return userId
     * @date 2023/4/16 0:09
     */
    public Mono<String> validateToken(String token) {
        return template.opsForValue().get(SESSION_KEY + token).map(id -> {
            template.expire(SESSION_KEY + token, Duration.ofMinutes(30));
            return id;
        }).switchIfEmpty(Mono.empty());
    }

    /**
     * create a token
     * @param userId
     * @return
     */
    public Mono<String> createNewToken(String userId) {
        String token = IdWorker.uuid();
        return template.opsForValue().set(SESSION_KEY + token, userId)
            .flatMap(b -> b ? template.expire(SESSION_KEY + token, Duration.ofMinutes(30)) : Mono.just(false))
            .flatMap(b -> b ? Mono.just(token) : Mono.empty());
    }


    public Mono<Boolean> expire(String token) {
        return template.delete(SESSION_KEY + token).map(l -> l > 0);
    }
}
