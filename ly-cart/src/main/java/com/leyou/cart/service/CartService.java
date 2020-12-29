package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    static final String KEY_PREFIX = "leyou:cart:uid:";

    @Autowired
    private GoodsClient goodsClient;
    public void addCart(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long id = userInfo.getId();
        String key = KEY_PREFIX+ id;
        BoundHashOperations<String, Object, Object>  hashOps = redisTemplate.boundHashOps(KEY_PREFIX + id);
        Boolean boo = hashOps.hasKey(cart.getSkuId().toString());
        //先提取出cart中的num
        Integer num = cart.getNum();
        if(boo){
            //存在商品
             String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
             cart = JsonUtils.toBean(cartJson, Cart.class);
             cart.setNum(cart.getNum()+num);
        }else{
            Sku sku = goodsClient.querySkuBySkuId(cart.getSkuId());
            cart.setUserId(id);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.strip(sku.getImages(),","));
        }
        hashOps.put(cart.getSkuId().toString(),JsonUtils.toString(cart));

    }

    public List<Cart> queryCart() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId().toString();
        if(!redisTemplate.hasKey(key)){
            return null;
        }
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        List<Object> carts = hashOps.values();
        if(CollectionUtils.isEmpty(carts)){
            return null;
        }
        return carts.stream().map(cart ->
                JsonUtils.toBean(cart.toString(),Cart.class)).collect(Collectors.toList());



    }

    public void updateCart(Cart cart) {
        // 获取登陆信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        // 获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(key);
        // 获取购物车信息
        String cartJson = hashOperations.get(cart.getSkuId().toString()).toString();
        Cart cart1 = JsonUtils.toBean(cartJson, Cart.class);
        // 更新数量
        cart1.setNum(cart.getNum());
        // 写入购物车
        hashOperations.put(cart.getSkuId().toString(), JsonUtils.toString(cart1));

    }

    public void deleteCartBySkuId(Long skuId) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId().toString());
        hashOps.delete(skuId.toString());



    }
}
