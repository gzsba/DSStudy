package com.pinyougou.solrutils;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import entity.SolrItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.solrutils
 * @date 2018-11-8
 */
@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    //查询所有sku列表
    public void importData(){
        TbItem where = new TbItem();
        where.setStatus("1");  //只查询已启用数据
        List<TbItem> itemList = itemMapper.select(where);
        System.out.println("查询数据完毕，正在组装数据与导入索引库....");

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
        System.out.println("数据组装完毕，开始导入索引库...");

        solrTemplate.saveBeans(solrItemList);
        solrTemplate.commit();

    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = context.getBean(SolrUtil.class);
        solrUtil.importData();
    }
}
