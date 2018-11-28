package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.page.service.impl
 * @date 2018-11-13
 */
public class PageMessageListener implements MessageListener{

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage msg = (ObjectMessage) message;
            if (msg != null && msg.getObject() != null) {
                Long[] ids = (Long[]) msg.getObject();
                //开始生成页面
                for (Long id : ids) {
                    itemPageService.genItemHtml(id);
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
