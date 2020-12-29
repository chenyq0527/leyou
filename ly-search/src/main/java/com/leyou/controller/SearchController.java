package com.leyou.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;
import com.leyou.pojo.SearchResult;
import com.leyou.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("page")
    public ResponseEntity<SearchResult> queryGoodsByPage(@RequestBody SearchRequest searchRequest){
        SearchResult goodsPageResult = searchService.queryGoodsByPage(searchRequest);
        if(goodsPageResult == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(goodsPageResult);

    }
}
