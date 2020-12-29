package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.pojo.Stock;
import com.leyou.mapper.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class GoodService {
    @Autowired
    private SpuMapper spuMapper;
    
    @Autowired
    private BrandMapper brandMapper;

   @Autowired
   private CategoryService categoryService;

   @Autowired
   private SkuMapper skuMapper;

   @Autowired
   private StockMapper stockMapper;

   @Autowired
   private SpuDetailMapper spuDetailMapper;

   @Autowired
   private AmqpTemplate amqpTemplate;

    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows){
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        if(saleable != null){
            criteria.orEqualTo("saleable",saleable);
        }
        PageHelper.startPage(page,rows);
        List<Spu> spus = spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        List<SpuBo> spuBos = new ArrayList<>();
       spus.forEach(spu -> {
           SpuBo spuBo = new SpuBo();
           BeanUtils.copyProperties(spu,spuBo);
           List<String> names = categoryService.querNamesByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
           spuBo.setCname(StringUtils.join(names,"/"));
           spuBo.setBname(brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());
           spuBos.add(spuBo);
       });
       return new PageResult<>(pageInfo.getTotal(),spuBos);

    }
    @Transactional
    public void saveGoods(SpuBo spu){
        spu.setId(null);
       spu.setCreateTime(new Date());
       spu.setLastUpdateTime(new Date());
       spu.setSaleable(true);
       spu.setValid(true);
       spuMapper.insertSelective(spu);


        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insertSelective(spuDetail);

        saveSkusAndSotck(spu);
        sendMessage(spu.getId(),"insert");
    }
    public void saveSkusAndSotck(SpuBo spuBo){
        List<Sku> skus = spuBo.getSkus();
        for(Sku sku : skus){
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(new Date());
            sku.setSpuId(spuBo.getId());
            skuMapper.insertSelective(sku);

            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockMapper.insertSelective(stock);

        }
    }
    public SpuDetail querySpuDetailBySpuId(Long id){
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        return spuDetail;

    }
    public List<Sku> querySkuBySpuId(Long spu_id){
        Sku sku = new Sku();
        sku.setSpuId(spu_id);
        List<Sku> skus = skuMapper.select(sku);
        for (Sku s:skus) {
            s.setStock(stockMapper.selectByPrimaryKey(s.getId()).getStock());
        }
        return skus;

    }
    @Transactional
    public void updateGoods(SpuBo spuBo){
        //删除原有的库存和sku
        List<Sku> skus = this.querySkuBySpuId(spuBo.getId());

        if(!CollectionUtils.isEmpty(skus)){
            List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
            Example example = new Example(Stock.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("skuId",ids);
            this.stockMapper.deleteByExample(example);
            Sku sku = new Sku();
            sku.setSpuId(spuBo.getId());
            skuMapper.delete(sku);

        }

        saveSkusAndSotck(spuBo);

        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setSaleable(null);
        spuBo.setValid(null);
        spuMapper.updateByPrimaryKeySelective(spuBo);
        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());
        try{
            sendMessage(spuBo.getId(),"update");
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public Spu querySpuById(Long id){
        return this.spuMapper.selectByPrimaryKey(id);

    }
    private void sendMessage(Long id,String type){
        try {
            amqpTemplate.convertAndSend("item."+type,id);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Sku querySkuBySkuId(Long skuId) {
        Sku sku = skuMapper.selectByPrimaryKey(skuId);
        return sku;
    }
}
