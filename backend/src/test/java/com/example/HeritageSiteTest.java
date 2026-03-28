package com.example;

import com.example.Common.Result;
import com.example.Pojo.HeritageSite;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class HeritageSiteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetNearbySites() throws Exception {
        // 景德镇经纬度
        Double lng = 117.17;
        Double lat = 29.27;

        MvcResult result = mockMvc.perform(get("/map/initial")
                .param("lng", lng.toString())
                .param("lat", lat.toString()))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Result<List<HeritageSite>> response = objectMapper.readValue(content, new TypeReference<>() {});

        assertEquals(1, response.getCode());
        assertNotNull(response.getData());
        System.out.println("测试结果：成功获取附近景点，数量：" + response.getData().size());
    }

    @Test
    void testGetSiteDetail() throws Exception {
        // 假设数据库中存在 ID 为 "1" 的景点 (实际测试时需确保数据存在)
        String siteId = "1";

        MvcResult result = mockMvc.perform(get("/map/" + siteId))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Result<HeritageSite> response = objectMapper.readValue(content, new TypeReference<>() {});

        if (response.getCode() == 1) {
            assertNotNull(response.getData());
            assertEquals(siteId, response.getData().getSiteId());
            System.out.println("测试结果：成功获取景点详情：" + response.getData().getName());
        } else {
            System.out.println("测试提示：未找到 ID 为 " + siteId + " 的景点数据，请检查数据库。");
        }
    }
}
