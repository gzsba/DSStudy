package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.File;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.page.service.impl
 * @date 2018-11-13
 */
public class PageDeleteMessageListener implements MessageListener {

    @Value("${PAGE_ITEM_DESC_HTML_URL}")
    private String PAGE_ITEM_DESC_HTML_URL;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage msg = (ObjectMessage) message;
            if (msg != null && msg.getObject() != null) {
                Long[] ids = (Long[]) msg.getObject();

                for (Long id : ids) {
                    System.out.println("正在删除goodsId为:" + id + "的静态详情页");
                    File beDel = new File(PAGE_ITEM_DESC_HTML_URL + id + ".html");
                    //如果文件存在
                    if (beDel.exists()) {
                        boolean flag = beDel.delete();
                        System.out.println("删除goodsId为:" + id + "的静态详情页，结果为：" + flag);
                    }
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
