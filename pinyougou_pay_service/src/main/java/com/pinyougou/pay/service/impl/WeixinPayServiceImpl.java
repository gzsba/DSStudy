package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.utils.HttpClient;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.pay.service.impl
 * @date 2018-11-21
 */
@Service(timeout = 5000)
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${notifyurl}")
    private String notifyurl;
    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //结果集
        Map map = new HashMap();
        try {
            //组装微信统一下单接口的入参
            Map param = new HashMap();
            param.put("appid", appid);  //appid
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //商户号
            param.put("body", "品优购");  //商品描述
            param.put("out_trade_no", out_trade_no);  //商户订单号
            param.put("total_fee", total_fee);  //支付金额
            param.put("spbill_create_ip", "127.0.0.1");  //终端IP
            param.put("notify_url", notifyurl);  //通知地址，微信回调的地址
            param.put("trade_type", "NATIVE");  //支付类型:通JSAPI JSAPI支付 NATIVE Native支付 APP APP支付

            //后续要处理签名问题， 这一步会生成一个带签名的xml字符
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发起统一下单请求.入参为： " + xmlParam);
            //发起统一下单接口
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);  //使用加密
            httpClient.setXmlParam(xmlParam);  //设置请求参数
            httpClient.post(); //发起post请求
            String xmlResult = httpClient.getContent();  //接收结果
            System.out.println("请求已响应，内容为：" + xmlResult);
            //解析结果
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);
            //返回参数包装
            map.put("code_url", resultMap.get("code_url"));  //生成二维码内容
            map.put("out_trade_no", out_trade_no);  //返回订单号
            map.put("total_fee", total_fee);  //应付金额

            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //组装微信查询订单接口的入参
            Map param = new HashMap();
            param.put("appid", appid);  //appid
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //商户号
            param.put("out_trade_no", out_trade_no);  //商户订单号
            //后续要处理签名问题， 这一步会生成一个带签名的xml字符
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发起查询订单状态请求，入参为： " + xmlParam);
            //发起查询订单接口
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);  //使用加密
            httpClient.setXmlParam(xmlParam);  //设置请求参数
            httpClient.post(); //发起post请求
            String xmlResult = httpClient.getContent();  //接收结果
            System.out.println("请求已响应，内容为：" + xmlResult);
            //解析结果
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map closePay(String out_trade_no) {
        try {
            //组装微信查询订单接口的入参
            Map param = new HashMap();
            param.put("appid", appid);  //appid
            param.put("mch_id", partner);  //商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //商户号
            param.put("out_trade_no", out_trade_no);  //商户订单号
            //后续要处理签名问题， 这一步会生成一个带签名的xml字符
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发起查询订单状态请求，入参为： " + xmlParam);
            //发起查询订单接口
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);  //使用加密
            httpClient.setXmlParam(xmlParam);  //设置请求参数
            httpClient.post(); //发起post请求
            String xmlResult = httpClient.getContent();  //接收结果
            System.out.println("请求已响应，内容为：" + xmlResult);
            //解析结果
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
