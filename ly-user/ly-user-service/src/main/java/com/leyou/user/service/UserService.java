package com.leyou.user.service;

import com.leyou.common.utils.CodecUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    static final String KEY_PREFIX = "user:code:phone:";

    static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public Boolean checkUserData(String data, Integer type) {
        User record = new User();
        switch (type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                return null;
        }
        return this.userMapper.selectCount(record) == 0;
    }
    public Boolean sendVerifyCode(String phone){
        String code = NumberUtils.generateCode(6);
        try{
            Map<String,String> msg = new HashMap<>();
            msg.put("phone",phone);
            msg.put("code",code);
            this.amqpTemplate.convertAndSend("leyou.sms.exchange","sms.verify.code",msg);
            redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5,TimeUnit.MINUTES);
            return true;
        }catch (Exception e){
            logger.error("发送短信失败。phone:{},code:{}",phone,code);
            return false;
        }
    }

    public Boolean Register(User user, String code) {
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if(!StringUtils.equals(cacheCode,code)){
            return false;
        }
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        String password = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(password);

        user.setCreated(new Date());
        user.setId(null);

        Boolean b = userMapper.insertSelective(user)==1;
        if(b){
            redisTemplate.delete(KEY_PREFIX  + user.getPhone());
        }
        return b;

    }

    public User queryUserByUserNameAndPassword(String username, String password) {
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        if(user == null) return null;
        if(!user.getPassword().equals(CodecUtils.md5Hex(password,user.getSalt()))) return null;
        return user;

    }
}
