package com.leyou.web;

import com.leyou.service.GoodsHtmlService;
import com.leyou.service.GoodsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@Controller
public class GoodsWebController {
    @Autowired
    private GoodsPageService goodsPageService;

    @Autowired
    private GoodsHtmlService goodsHtmlService;


    @GetMapping("item/{id}.html")
    public String toItemPage(Model model, @PathVariable("id")Long id){
        Map<String,Object> map  =  goodsPageService.loadModel(id);
        model.addAllAttributes(map);
        this.goodsHtmlService.asyncExcute(id);
        return "item";
    }

}
