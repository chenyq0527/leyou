package com.leyou.controller;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam("pid") Long pid){
        if(pid == null || pid.longValue() < 0){
            return  ResponseEntity.badRequest().build();
        }
        List<Category> list = categoryService.queryCategoryByPid(pid);
        if(CollectionUtils.isEmpty(list)){
            return  ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);


    }
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoryByBid(@PathVariable("bid")Long bid){
        List<Category> categories = this.categoryService.queryCategoryByBid(bid);
        if(categories == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categories);

    }
    @GetMapping("names")
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids") List<Long> ids){
        List<String> names = categoryService.querNamesByIds(ids);
        if(CollectionUtils.isEmpty(names)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(names);

    }

    @GetMapping("all/level")
    public ResponseEntity<List<Category>> queryCategoryByCid(@RequestParam("id")Long cid){
        List<Category> categories = categoryService.queryAllByCid3(cid);
        if(CollectionUtils.isEmpty(categories)){
            return  ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categories);
    }


}
