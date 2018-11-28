package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.utils.IdWorker;
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
    private OrderService orderService;

    @RequestMapping("createNative")
    public Map createNative(){
        /*IdWorker worker = new IdWorker();

        return weixinPayService.createNative(worker.nextId() + "", "1");*/

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        if(payLog != null){
            return weixinPayService.createNative(payLog.getOutTradeNo() + "", payLog.getTotalFee() + "");
        }else{
            return new HashMap();
        }
    }

    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result = null;
        try {
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
                    orderService.updateOrderStatus(out_trade_no,map.get("transaction_id").toString());
                    break;
                }
                //3秒发起一次查询请求
                Thread.sleep(3000);
                x++;

                //如果用户5分钟都没有支付，提示支付超时
                if(x > 100){
                    result = new Result(false, "支付已超时");
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
