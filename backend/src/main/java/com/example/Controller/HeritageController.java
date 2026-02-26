package com.example.Controller;

import com.example.Common.Result;
import com.example.Service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}