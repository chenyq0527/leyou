package com.leyou.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodController  {
    @Autowired
    private GoodService goodService;
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(@RequestParam(value = "key",required = false) String key,
                                                                  @RequestParam(value = "saleable",required = false) Boolean saleable,
                                                                  @RequestParam(value = "page",defaultValue = "1") Integer page,
                                                                  @RequestParam(value = "rows",defaultValue = "5")Integer rows){
        PageResult<SpuBo> PageResult = goodService.querySpuByPage(key, saleable, page, rows);
        if(CollectionUtils.isEmpty(PageResult.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PageResult);
    }
    @PostMapping("goods")
    public ResponseEntity<Void> saveSpu(@RequestBody SpuBo spuBo){
        this.goodService.saveGoods(spuBo);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    @GetMapping("spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("id")Long id){
        SpuDetail spuDetail = goodService.querySpuDetailBySpuId(id);
        if(spuDetail == null){
            ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDetail);

    }
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id")Long spu_id){
        List<Sku> skus = goodService.querySkuBySpuId(spu_id);
        if(CollectionUtils.isEmpty(skus)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skus);
    }
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        goodService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@RequestParam("id")Long id){
        Spu spu = this.goodService.querySpuById(id);
        if(spu == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spu);
    }
    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId")Long skuId){
        Sku sku = this.goodService.querySkuBySkuId(skuId);
        if(sku == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sku);

    }
}
