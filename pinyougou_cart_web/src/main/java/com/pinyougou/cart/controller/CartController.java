package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.grouppojo.Cart;
import com.pinyougou.utils.CookieUtil;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.cart.controller
 * @date 2018-11-18
 */
@RestController
@RequestMapping("cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @RequestMapping("findCartList")
    public List<Cart> findCartList(){
        //获取登录名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Cart> cookieCartList = new ArrayList<>();
        //查询cookies中的购物车列表
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", true);
        //找到了购物车列表
        if (StringUtils.isNotBlank(cartListStr)) {
            cookieCartList = JSON.parseArray(cartListStr, Cart.class);
        }
        //如果用户没用登录
        if("anonymousUser".equals(username)){
            System.out.println("从cookie中获取了购物车数据.....");
            return cookieCartList;
        }else{  //用户已登录
            System.out.println("从Redis中获取了购物车数据.....");
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            //合并购物车
            if(cookieCartList.size() > 0){
                System.out.println("合并了购物车...");
                cartListFromRedis = cartService.mergeCartList(cookieCartList, cartListFromRedis);
                //保存到redis中
                cartService.saveCartListToRedis(username, cartListFromRedis);
                //清空cookie购物车
                CookieUtil.deleteCookie(request,response,"cartList");
            }
            return cartListFromRedis;
        }
    }

    @RequestMapping("addGoodsToCartList")
    //默认情况下@CrossOrigin是允许操作cookie，所以allowCredentials = "true"可以缺省
    @CrossOrigin(origins = "http://localhost:8085",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId,Integer num){
        try {
            //设置可以访问的域，值设置为*时，允许所有域
            //response.setHeader("Access-Control-Allow-Origin", "http://localhost:8085");
            //如果需要操作cookies，必须加上此配置，标识服务端可以写cookies，
            // 并且Access-Control-Allow-Origin不能设置为*，因为cookies操作需要域名
            //response.setHeader("Access-Control-Allow-Credentials", "true");

            //获取登录名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            //查询当前的购物车列表
            List<Cart> cartList = this.findCartList();
            //追加购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //如果用户没用登录
            if("anonymousUser".equals(username)) {
                System.out.println("设置了cookie中购物车数据.....");
                //保存购物车到cookies
                String cartListStr = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", cartListStr, 60 * 60 * 24, true);
            }else{
                System.out.println("设置了redis中购物车数据.....");
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加购物车成功!");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "添加购物车失败！");
    }
}
