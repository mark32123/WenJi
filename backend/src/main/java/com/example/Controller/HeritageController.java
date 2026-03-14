package com.example.Controller;

import com.example.Common.Result;
import com.example.Pojo.HeritageSite;
import com.example.Service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/map")
@Tag(name = "历史遗迹", description = " 历史遗迹相关接口")
public class HeritageController {
    @Autowired
    private SiteService siteService;

    /**
     * 获取附近 HeritageSite
     * @param lng
     * @param lat
     * @return
     */
    @Operation(summary = "获取附近历史遗迹", description = "获取附近历史遗迹")
    @GetMapping("/initial")
    public ResponseEntity<?> getNearby(@RequestParam Double lng, @RequestParam Double lat) {
        return ResponseEntity.ok(Result.success(siteService.getNearbyHeritageSites(lng, lat)));
    }

    /**
     * 根据ID获取景点详细介绍
     * * @param id 景点ID
     * @return 包含介绍、历史、图片的详情对象
     */
    @GetMapping("/{id}")
    public Result<HeritageSite> getSiteDetail(@PathVariable("id") String id) {
        HeritageSite detail = siteService.getDetail(id);
        if (detail != null) {
            return Result.success(detail);
        }
        return Result.error("未找到该景点的详细信息");
    }

}