package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.search.service.impl
 * @date 2018-11-13
 */
public class SolrDeleteMessageListener implements MessageListener{
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            //获取消息内容
            ObjectMessage msg = (ObjectMessage) message;
            if(msg != null && msg.getObject() != null) {
                Long[] ids = (Long[]) msg.getObject();

                //调用删除索引库
                itemSearchService.deleteByGoodsIds(ids);

            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
