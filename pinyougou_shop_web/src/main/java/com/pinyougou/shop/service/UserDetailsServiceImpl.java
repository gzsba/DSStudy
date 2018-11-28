package com.pinyougou.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 扩展认证权限信息
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.shop.service
 * @date 2018-10-31
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username + "，进入了UserDetailsServiceImpl.loadUserByUsername...");

        //创建角色列表
        List<GrantedAuthority> authorities = new ArrayList<>();
        //添加受权角色
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //对接服务，查询商家，返回密码
        TbSeller seller = sellerService.findOne(username);
        //如果查找不到用户，或者商家状态为非审核通过状态，不允许登录
        if(seller == null || !"1".equals(seller.getStatus())){
            //返回空，认证失败
            return null;
        }else{
            /*//只要用户输入的密码是123456，都可以放行
            return new User(username,"123456",authorities); */

            //返回数据库的密码
            return new User(username,seller.getPassword(),authorities);
        }

    }
}
