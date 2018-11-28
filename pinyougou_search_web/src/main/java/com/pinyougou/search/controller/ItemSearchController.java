package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.search.controller
 * @date 2018-11-8
 */
@RestController
@RequestMapping("itemsearch")
public class ItemSearchController {
    @Reference
    private ItemSearchService itemSearchService;

    @RequestMapping("search")
    public Map search(@RequestBody Map searchMap){
        return itemSearchService.search(searchMap);
    }
}
