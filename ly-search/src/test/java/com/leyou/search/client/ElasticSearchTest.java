package com.leyou.search.client;

import com.leyou.LySearchApplication;
import com.leyou.client.BrandClient;
import com.leyou.client.GoodsClient;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Spu;
import com.leyou.pojo.Goods;
import com.leyou.repository.GoodsRepository;
import com.leyou.service.IndexService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class ElasticSearchTest {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private IndexService indexService;

    @Test
    public void createIndex() {
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);
        Integer page = 1;
        Integer rows = 100;
        do {
            PageResult<SpuBo> result = goodsClient.querySpuByPage(null, true, page, rows);
            List<Goods> goodList = result.getItems().stream().map(spuBo -> {
                try {
                    return this.indexService.buildGoods( spuBo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodList);
            page = result.getItems().size();
            rows++;
        } while (rows == 100);
    }
}
