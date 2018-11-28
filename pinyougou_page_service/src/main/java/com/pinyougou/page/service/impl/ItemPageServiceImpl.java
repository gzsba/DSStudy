package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.page.service.impl
 * @date 2018-11-12
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Value("${PAGE_ITEM_DESC_HTML_URL}")
    private String PAGE_ITEM_DESC_HTML_URL;

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //获取Freemarker操作对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //生成静态页面
            Template template = configuration.getTemplate("item.ftl");
            //构建数据模型
            Map map = new HashMap();
            //查询商品spu信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            map.put("goods", goods);
            //查询商品扩展信息
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc", goodsDesc);

            //组装三级分类面包屑
            String category1Name = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String category2Name = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String category3Name = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            map.put("category1Name", category1Name);
            map.put("category2Name", category2Name);
            map.put("category3Name", category3Name);

            //查询商品sku信息
            Example example = new Example(TbItem.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status", "1");  //只查询启用的商品
            criteria.andEqualTo("goodsId", goodsId);  //相应的sku
            example.setOrderByClause("isDefault DESC");  //让默认的sku在第一条
            List<TbItem> itemList = itemMapper.selectByExample(example);
            map.put("itemList", itemList);

            //创建输出流
            Writer out = new FileWriter(PAGE_ITEM_DESC_HTML_URL + goodsId + ".html");

            //输出静态文件
            template.process(map, out);
            //释放资源
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
