package bos_fore.test;

import java.util.concurrent.TimeUnit;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class Test {
	
	@Autowired
	private RedisTemplate<String , String > redisTemplate;
	@org.junit.Test
	public void test() {
		redisTemplate.opsForValue().set("lisi", "1er 2234",30,TimeUnit.SECONDS);
		System.out.println(redisTemplate.opsForValue().get("lisi"));
	}
}
