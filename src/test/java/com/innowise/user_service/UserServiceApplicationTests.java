package com.innowise.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class UserServiceApplicationTests {


	@MockitoBean
	private RedisConnectionFactory redisConnectionFactory;

	@MockitoBean
	private RedisTemplate<?, ?> redisTemplate;

	@Test
	void contextLoads() {
	}
}
