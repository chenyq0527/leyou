package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.*;
import com.leyou.pojo.Goods;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndexService {
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;
    
    @Autowired
    private BrandClient brandClient;
    
    @Autowired
    private SpecificationClient specificationClient;

    public Goods buildGoods(SpuBo spu){
        Long id = spu.getId();
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        List<String> names = categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        List<Sku> skus = goodsClient.querySkuBySpuId(id);
        List<Long> prices = new ArrayList<>();
        List<Map<String,Object>> skuMapList = new ArrayList<>();

        for(Sku sku : skus){
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            skuMap.put("image",StringUtils.isNotBlank(sku.getImages()) ? StringUtils.split(sku.getImages(),","):"");
            prices.add(sku.getPrice());
            skuMapList.add(skuMap);

        }
        List<SpecParam> specParams = specificationClient.querySpecParamById(null, spu.getCid3(), null);
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(id);
        Map<Long,String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class,String.class);
        Map<Long,List<Object>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>() {
        });
        Map<String,Object> specs = new HashMap<>();
        specParams.forEach(specParam ->{
           String key = specParam.getName();
           Object value = "";
           if(specParam.getGeneric()){
               value = genericSpec.get(specParam.getId());
               if(specParam.getNumeric()){
                   value = chooseSegment(value.toString(),specParam);
               }
           }else{
               value = specialSpec.get(specParam.getId());
           }
            value = (value == null ? "其他" : value);
           specs.put(key,value);
        });
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setId(id);
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        goods.setAll(spu.getTitle() + StringUtils.join(names, " ") + brand.getName() );
        goods.setPrice(prices);
        goods.setSkus(JsonUtils.toString(skuMapList));
        goods.setSpecs(specs);
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
}
