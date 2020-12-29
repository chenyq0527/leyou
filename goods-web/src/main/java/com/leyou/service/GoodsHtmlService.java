package com.leyou.service;

import com.leyou.utils.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Logger;

@Slf4j
@Service
public class GoodsHtmlService {
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private GoodsPageService goodsPageService;

    public void creatHtml(Long spuId){
        PrintWriter printWriter = null;

        try {
            Map<String, Object> map = goodsPageService.loadModel(spuId);
            //创建thymeleaf上下文
            Context context = new Context();
            //把数据放下上下文
            context.setVariables(map);
            File file = new File("E:\\upload\\"+spuId+".html");
            if (file.exists()) {
                file.delete();
            }
            printWriter = new PrintWriter(file);
            //执行静态化方法
            templateEngine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();;
        }finally {
            if(printWriter != null)
                printWriter.close();
        }


    }
    public void deleteHtml(Long id){
        File file = new File("E:\\upload\\"+id+".html");
        file.deleteOnExit();
    }
    /**
     * 新建线程处理页面静态化
     * @param spuId
     */
    public void asyncExcute(Long spuId) {
        ThreadUtils.execute(()->creatHtml(spuId));
        /*ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                createHtml(spuId);
            }
        });*/
    }
}
