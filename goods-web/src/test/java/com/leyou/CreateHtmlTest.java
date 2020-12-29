package com.leyou;

import com.leyou.service.GoodsHtmlService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CreateHtmlTest {
    @Autowired
    private GoodsHtmlService goodsHtmlService;
    @Test
    public void  creatHtml(){
        goodsHtmlService.creatHtml(94L);
    }
}
