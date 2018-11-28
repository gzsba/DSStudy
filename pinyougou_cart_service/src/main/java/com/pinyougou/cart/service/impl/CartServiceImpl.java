package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.grouppojo.Cart;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.cart.service.impl
 * @date 2018-11-18
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item == null){
            throw new RuntimeException("商品信息不存在");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = this.searchCartBySellerId(cartList,sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if(cart == null){
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());  //商家名称
            //构建购物车商品详情信息
            List<TbOrderItem> orderItemList = new ArrayList<>();
            //设置详情
            TbOrderItem orderItem = createOrderItem(num, item);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        //5.如果购物车列表中存在该商家的购物车
        }else{
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = this.searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            //5.1. 如果没有，新增购物车明细
            if(orderItem == null){
                //设置详情
                orderItem = createOrderItem(num, item);
                cart.getOrderItemList().add(orderItem);
            }else{
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);
                //计算小计
                double totalFee = orderItem.getPrice().doubleValue() * orderItem.getNum();
                orderItem.setTotalFee(new BigDecimal(totalFee));

                //修改数量后，如果购买数量小于1，删除当前的商品购买信息
                if(orderItem.getNum() < 1){
                    cart.getOrderItemList().remove(orderItem);
                }
                //删除完商品后，如果当前商家，已经没有要购买的商品,删除整个商家的购物车信息
                if(cart.getOrderItemList().size() < 1){
                    cartList.remove(cart);
                }
            }

        }
        return cartList;
    }

    /**
     * 查询当前商品列表中，有没有添加过当前入参的商品信息
     * @param orderItemList 当前商家的商品列表
     * @param itemId 将要购买的商品sku-id
     * @return 查找到的商品信息，返回null，说明没有添加过
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if(itemId.longValue() == orderItem.getItemId().longValue()){
                return orderItem;
            }
        }
        return  null;
    }

    /**
     * 构建购物车商品详情信息
     * @param num
     * @param item
     * @return
     */
    private TbOrderItem createOrderItem(Integer num, TbItem item) {
        if(num < 1){
            throw new RuntimeException("请别搞事，要传入正确的购买数量！");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setNum(num);  //购买数量
        orderItem.setSellerId(item.getSellerId());
        orderItem.setGoodsId(item.getGoodsId());  //spu-id
        orderItem.setItemId(item.getId());   //sku-id
        orderItem.setPicPath(item.getImage()); //图片
        orderItem.setPrice(item.getPrice());  //单价
        orderItem.setTitle(item.getTitle());
        //计算小计
        double totalFee = orderItem.getPrice().doubleValue() * num;
        orderItem.setTotalFee(new BigDecimal(totalFee));
        return orderItem;
    }

    /**
     * 查询当前用户有没有添加过入参商家的商品
     * @param cartList 当前购物车列表
     * @param sellerId 商家的id
     * @return 查找到的购物车对象，为null时，说明没找到
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if(sellerId.equals(cart.getSellerId())){
                return cart;
            }
        }
        return null;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList1) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                this.addGoodsToCartList(cartList2, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList2;
    }


}
