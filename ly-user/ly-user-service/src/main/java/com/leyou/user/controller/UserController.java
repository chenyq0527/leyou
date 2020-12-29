package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data")String data,@PathVariable("type")Integer type){
        Boolean boo = this.userService.checkUserData(data, type);
        if(boo == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(boo);
    }
    @PostMapping("code")
    public ResponseEntity<Boolean> sendVerifyCode(@RequestParam("phone") String phone){
        Boolean boo = this.userService.sendVerifyCode(phone);
        if(boo == null || !boo){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.noContent().build();

    }
    @PostMapping("register")
    public ResponseEntity<Void> Register(@Valid User user, @RequestParam("code")String code){
        Boolean boo = this.userService.Register(user, code);
        if(boo == null || !boo){
            return ResponseEntity.badRequest().build();
        }
        return  new ResponseEntity<>(HttpStatus.CREATED);
    }
    @GetMapping("query")
    public ResponseEntity<User> queryUserByUserNameAndPassword(@RequestParam("username")String username,
                                                               @RequestParam("password")String password){
        User user = this.userService.queryUserByUserNameAndPassword(username, password);
        if(user == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
