package com.pinyougou.user.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringSecurity认证扩展类
 * @author Steven
 * @version 1.0
 * @description com.itheima.demo.service
 * @date 2018-11-16
 */
public class UserDetailServiceImpl implements UserDetailsService{
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //声明角色列表
        List<GrantedAuthority> authorties = new ArrayList<GrantedAuthority>();
        authorties.add(new SimpleGrantedAuthority("ROLE_USER"));
        //这里返回的密码应该为空，因为认证，交给cas完成
        return new User(username,"",authorties);
    }
}
