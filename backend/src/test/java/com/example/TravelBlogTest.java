package com.example;

import com.example.Exception.BusinessException;
import com.example.Pojo.TravelBlog;
import com.example.Service.TravelBlogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TravelBlogTest {

    @Autowired
    private TravelBlogService blogService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRepeatPublishWithDistributedLock() {
        // 1. 构造唯一的测试博文数据
        String uniqueTitle = "西安城墙_" + UUID.randomUUID().toString().substring(0, 8);
        TravelBlog blog = new TravelBlog();
        blog.setUserId(1); 
        blog.setTitle(uniqueTitle);
        blog.setContent("今日登临古城墙，顿感历史之厚重...");
        blog.setSiteId("101"); 

        String lockKey = "lock:blog:publish:1:" + uniqueTitle;
        System.out.println("Debug - Lock Key: " + lockKey);

        // 2. 模拟第一个请求成功处理
        try {
            blogService.publishBlog(blog);
        } catch (Exception e) {
            fail("首发请求应处理成功, 却报错: " + e.getMessage());
        }
        
        // 验证锁确实已经设置 (手动检查 Redis)
        Boolean hasKey = stringRedisTemplate.hasKey(lockKey);
        System.out.println("Debug - Lock Key Exists after first call: " + hasKey);
        assertTrue(Boolean.TRUE.equals(hasKey), "第一个请求后，分布式锁应存在于 Redis 中");
        
        // 3. 模拟在短时间内发起的第二次重复请求
        blog.setBlogId(null); 

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            blogService.publishBlog(blog);
        });
        
        // 4. 校验异常提示信息是否符合幂等拦截要求 (429 Too Many Requests)
        assertEquals(429, exception.getCode());
        String expectedMessage = "系统正在处理中，请勿重复提交";
        assertTrue(exception.getMessage().contains(expectedMessage));
        
        System.out.println("测试结果：分布式锁成功拦截重复请求，数据库记录唯一性得到保障。");
        
        // 5. 延迟清理锁
        stringRedisTemplate.delete(lockKey);
    }
}
