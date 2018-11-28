package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 索引库变更监听器
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.search.service.impl
 * @date 2018-11-13
 */
public class SolrMessageListener implements MessageListener{

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage msg = (TextMessage) message;
            if(msg != null && StringUtils.isNotBlank(msg.getText())) {
                List<TbItem> itemList = JSON.parseArray(msg.getText(), TbItem.class);

                List<SolrItem> solrItemList = new ArrayList<>();
                SolrItem solrItem = null;
                for (TbItem item : itemList) {
                    solrItem = new SolrItem();
                    //复制所有相同的属性到SolrItem
                    BeanUtils.copyProperties(item, solrItem);

                    //把规格信息转成map
                    Map specMap = JSON.parseObject(item.getSpec(), Map.class);
                    solrItem.setSpecMap(specMap);

                    solrItemList.add(solrItem);
                }
                //导入索引库
                itemSearchService.importList(solrItemList);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
