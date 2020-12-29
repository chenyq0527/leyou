package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.controller.AuthController;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import jdk.nashorn.internal.runtime.regexp.JoniRegExp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class AuthService  {
    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties jwtProperties;


    public String authentication(String username,String password){
        try {
            User user = userClient.queryUserByUserNameAndPassword(username, password);
            if(user == null){
                return null ;
            }
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            String token = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            return  token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }


}
