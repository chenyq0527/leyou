package com.leyou.service;

import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodsPageService {
    @Autowired
    private GoodsClient goodsClient;
    
    @Autowired
    private BrandClient brandClient;
    
    @Autowired
    private CategoryClient categoryClient;
    
    @Autowired
    private SpecificationClient specificationClient;

    public Map<String,Object> loadModel(Long spuId){
        Map<String,Object> map = new HashMap<>();
        //查询spu
        SpuBo spu = goodsClient.querySpuById(spuId);
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //查询商品集合
        List<Category> categories = categoryClient.queryCategoryByBid(spu.getBrandId());
        //查询skus
        List<Sku> skus = goodsClient.querySkuBySpuId(spuId);
        //查询规格参数
        List<SpecGroup> specs = specificationClient.querySpecGroupByCid(spu.getCid3());
        SpuDetail detail = goodsClient.querySpuDetailBySpuId(spuId);
        map.put("title",spu.getTitle());
        map.put("subTitle",spu.getSubTitle());
        map.put("brand",brand);
        map.put("categories",categories);
        map.put("skus",skus);
        map.put("detail",detail);
        map.put("specs",specs);
        return map;
    }
}
