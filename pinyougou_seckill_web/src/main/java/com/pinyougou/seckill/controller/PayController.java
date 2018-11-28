package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.cart.controller
 * @date 2018-11-21
 */
@RestController
@RequestMapping("pay")
public class PayController {
    @Reference
    private WeixinPayService weixinPayService;
    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("createNative")
    public Map createNative(){
        /*IdWorker worker = new IdWorker();

        return weixinPayService.createNative(worker.nextId() + "", "1");*/

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        if(seckillOrder != null){
            long money = (long) (seckillOrder.getMoney().doubleValue() * 100);
            return weixinPayService.createNative(seckillOrder.getId() + "", money + "");
        }else{
            return new HashMap();
        }
    }

    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result = null;
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            int x = 0;
            //开始检测订单状态
            while (true){
                Map map = weixinPayService.queryPayStatus(out_trade_no);
                if(map == null ){
                    result = new Result(false, "支付失败！");
                    break;
                }
                //支付成功
                if(map.get("trade_state").equals("SUCCESS")){
                    result = new Result(true, "支付成功！");

                    //执行更新订单状态
                    //orderService.updateOrderStatus(out_trade_no,map.get("transaction_id").toString());
                    seckillOrderService.saveOrderFromRedisToDb(userId,new Long(out_trade_no),map.get("transaction_id").toString());
                    break;
                }
                //3秒发起一次查询请求
                Thread.sleep(3000);
                x++;

                //如果用户5分钟都没有支付，提示支付超时
                if(x > 100){
                    result = new Result(false, "支付已超时");

                    //1.调用微信的关闭订单接口（学员实现）
                    Map<String,String> payresult = weixinPayService.closePay(out_trade_no);
                    if( !"SUCCESS".equals(payresult.get("result_code")) ){//如果返回结果是正常关闭
                        if("ORDERPAID".equals(payresult.get("err_code"))){
                            result=new Result(true, "支付成功");
                            seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id").toString());
                        }
                    }
                    if(result.isSuccess()==false) {
                        System.out.println("超时，取消订单");
                        //2.调用删除
                        seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
                    }
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
