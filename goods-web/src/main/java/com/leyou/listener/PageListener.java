package com.leyou.listener;

import com.leyou.service.GoodsHtmlService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PageListener {
    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.create.web.queue",durable = "true"),
            exchange = @Exchange(value ="leyou.item.exchange",
                    ignoreDeclarationExceptions = "true",
            type = ExchangeTypes.TOPIC),
            key = {"item-insert","item-update"}
    ))
    public void createListener(Long id){
        if(id == null){
            return;
        }
        goodsHtmlService.creatHtml(id);
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.item.web.delete",durable = "true"),
            exchange = @Exchange(value = "leyou.item.exchange",
                    ignoreDeclarationExceptions = "true",
            type = ExchangeTypes.TOPIC),
            key = {"item-delete"}
    ))
    public void deleteListener(Long id){
        goodsHtmlService.deleteHtml(id);
    }
}
