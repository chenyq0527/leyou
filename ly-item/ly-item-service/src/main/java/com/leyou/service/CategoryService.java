package com.leyou.service;

import com.leyou.item.pojo.Category;
import com.leyou.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    //根据pid查询商品分类
    public List<Category> queryCategoryByPid(Long pid){
        Category category = new Category();
        category.setParentId(pid);
        List<Category> categories = categoryMapper.select(category);
        return categories;

    }

    public List<Category> queryCategoryByBid(Long bid){
        List<Category> categories = this.categoryMapper.queryCategoryIdByBid(bid);
        return categories;

    }
    public List<String> querNamesByIds(List<Long> ids){
        List<String> names = categoryMapper.selectByIdList(ids)
                .stream().map(Category::getName).collect(Collectors.toList());
        return names;
    }
    public List<Category> queryAllByCid3(Long cid3){
        Category c3 = categoryMapper.selectByPrimaryKey(cid3);
        Category c2 = categoryMapper.selectByPrimaryKey(c3.getParentId());
        Category c1 = categoryMapper.selectByPrimaryKey(c2.getParentId());
        List<Category> list = Arrays.asList(c1,c2,c3);
        return  list ;



    }

}
