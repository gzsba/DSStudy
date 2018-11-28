package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.search.service.impl
 * @date 2018-11-8
 */
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map search(Map searchMap) {
        Map result = new HashMap();
        //1、条件查询数据
        searchList(searchMap, result);
        //2、分组查询商品分类列表
        searchCategoryList(searchMap,result);
        //3、跟据分类名称，查询品牌与规格列表
        String category = searchMap.get("category") == null ? null : searchMap.get("category").toString();
        if(StringUtils.isNotBlank(category)){
            searchBrandAndSpecList(category,result);
        }else {
            List<String> categoryList = (List<String>) result.get("categoryList");
            if (categoryList.size() > 0) {
                searchBrandAndSpecList(categoryList.get(0), result);
            }
        }

        return result;
    }

    @Override
    public void importList(List itemList) {
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(Long[] goodsIdList) {
        /*方案一：循环
        for (Long goodsId : goodsIdList) {
            Query query = new SimpleQuery("item_goodsid:" + goodsId);
            solrTemplate.delete(query);
        }*/
        //推荐使用方案二
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    /**
     * 跟据分类名称，查询品牌与规格列表
     * @param category 分类的名称
     * @param result 结果map
     */
    private void searchBrandAndSpecList(String category, Map result) {
        //跟据分类名称查询模板id
        if(redisTemplate.boundHashOps("itemCat").get(category) != null) {
            Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

            //查询品牌列表
            List<Map> brandIds = (List<Map>) redisTemplate.boundHashOps("brandIds").get(typeId);
            result.put("brandIds", brandIds);
            //查询规格列表
            List<Map> specIds = (List<Map>) redisTemplate.boundHashOps("specIds").get(typeId);
            result.put("specIds", specIds);
        }
    }

    /**
     * 分组查询商品分类列表
     * @param searchMap 查询map
     * @param result 结果map
     */
    private void searchCategoryList(Map searchMap, Map result) {
        List<String> categoryList = new ArrayList<>();
        //1.创建查询条件对象query = new SimpleQuery()
        Query query = new SimpleQuery();
        //2.复制之前的Criteria组装查询条件的代码
        //组装查询条件
        String keywords = searchMap.get("keywords") == null ? null : searchMap.get("keywords").toString();
        //关键字条件
        if (StringUtils.isNotBlank(keywords)) {
            Criteria criteria = new Criteria("item_keywords").is(keywords);
            query.addCriteria(criteria);
        }
        //3.创建分组选项对象new GroupOptions().addGroupByField(域名)
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        //4.设置分组对象query.setGroupOptions
        query.setGroupOptions(groupOptions);
        //5.得到分组页对象page = solrTemplate.queryForGroupPage
        GroupPage<SolrItem> page = solrTemplate.queryForGroupPage(query, SolrItem.class);
        //6.得到分组结果集groupResult = page.getGroupResult(域名)
        GroupResult<SolrItem> groupResult = page.getGroupResult("item_category");
        //7.得到分组结果入口groupEntries = groupResult.getGroupEntries()
        Page<GroupEntry<SolrItem>> groupEntries = groupResult.getGroupEntries();
        //8.得到分组入口集合content = groupEntries.getContent()
        List<GroupEntry<SolrItem>> content = groupEntries.getContent();
        //9.遍历分组入口集合content.for(entry)，记录结果entry.getGroupValue()
        for (GroupEntry<SolrItem> entry : content) {
            categoryList.add(entry.getGroupValue());
        }
        //把分类列表返回前端
        result.put("categoryList", categoryList);
    }

    /**
     * 关键字搜索
     * @param searchMap
     * @param result
     */
    private void searchList(Map searchMap, Map result) {
        //2.构建query高亮查询对象new SimpleHighlightQuery
        HighlightQuery query = new SimpleHighlightQuery();
        //3.复制之前的Criteria组装查询条件的代码
        String keywords = searchMap.get("keywords") == null ? null : searchMap.get("keywords").toString();
        //3.1关键字条件
        if (StringUtils.isNotBlank(keywords)) {
            //去除空格
            keywords = keywords.replaceAll(" ", "");
            //把参数重置
            searchMap.put("keywords", keywords);
            Criteria criteria = new Criteria("item_keywords").is(keywords);
            query.addCriteria(criteria);
        }
        //3.2分类条件过滤
        String category = searchMap.get("category") == null ? null : searchMap.get("category").toString();
        if (StringUtils.isNotBlank(category)) {
            Criteria categoryCriteria = new Criteria("item_category").is(category);
            FilterQuery categoryQuery = new SimpleFilterQuery(categoryCriteria);
            query.addFilterQuery(categoryQuery);
        }
        //3.3品牌条件过滤
        String brand = searchMap.get("brand") == null ? null : searchMap.get("brand").toString();
        if (StringUtils.isNotBlank(brand)) {
            Criteria criteria = new Criteria("item_brand").is(brand);
            FilterQuery filterQuery = new SimpleFilterQuery(criteria);
            query.addFilterQuery(filterQuery);
        }
        //3.4规格过滤查询
        String spec = searchMap.get("spec") == null ? null : searchMap.get("spec").toString();
        if (StringUtils.isNotBlank(spec)) {
            //把规格json串转换成Map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);

            for (String key : specMap.keySet()) {
                Criteria criteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //3.5价格区间查询,前端传入的值为：0-500,3000-*
        String price = searchMap.get("price") == null ? null : searchMap.get("price").toString();
        if(StringUtils.isNotBlank(price)){
            //价格条件数组
            String[] split = price.split("-");
            //开始组装查询条件
            /* between不能处理*号问题，所以我们不用了
            Criteria criteria = new Criteria("item_price").between(split[0],split[1]);
            FilterQuery filterQuery = new SimpleFilterQuery(criteria);
            query.addFilterQuery(filterQuery);*/
            //匹配起始价格
            if(!"0".equals(split[0])){
                Criteria criteria = new Criteria("item_price").greaterThanEqual(split[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }
            //匹配结束价格
            if(!"*".equals(split[1])){
                Criteria criteria = new Criteria("item_price").lessThanEqual(split[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //3.6组装分页查询参数
        String pageNoStr = searchMap.get("pageNo") == null ? null : searchMap.get("pageNo").toString();
        Integer pageNo = 1;  //当前页
        if(StringUtils.isNotBlank(pageNoStr)){
            pageNo = new Integer(pageNoStr);
        }
        String pageSizeStr = searchMap.get("pageSize") == null ? null : searchMap.get("pageSize").toString();
        Integer pageSize = 40;  //每页查询的记录数
        if(StringUtils.isNotBlank(pageSizeStr)){
            pageSize = new Integer(pageSizeStr);
        }
        //设置起始行号
        query.setOffset((pageNo - 1) * pageSize);
        //设置查询的记录数
        query.setRows(pageSize);

        //3.7设置排序条件
        //排序方式ASC  DESC
        String sortValue = searchMap.get("sort") == null ? null : searchMap.get("sort").toString();
        String sortField = searchMap.get("sortField") == null ? null : searchMap.get("sortField").toString();
        if(StringUtils.isNotBlank(sortField)){
            //如果传入的是升序
            if("ASC".equals(sortValue.toUpperCase())){
                Sort sort = new Sort(Sort.Direction.ASC, "item_" +sortField);
                query.addSort(sort);
            }
            if("DESC".equals(sortValue.toUpperCase())){
                Sort sort = new Sort(Sort.Direction.DESC, "item_" +sortField);
                query.addSort(sort);
            }
        }

        //4.调用query.setHighlightOptions()方法，
        // 构建高亮数据三步曲：new HighlightOptions().addField(高亮业务域)，
        // .setSimpleP..(前缀)，.setSimpleP..(后缀)
        HighlightOptions hOprions = new HighlightOptions();
        hOprions.addField("item_title");
        hOprions.setSimplePrefix("<em style=\"color:red;\">");
        hOprions.setSimplePostfix("</em>");
        query.setHighlightOptions(hOprions);

        //1.调用solrTemplate.queryForHighlightPage(query,class)方法，高亮查询数据
        // 5.接收solrTemplate.queryForHighlightPage的返回数据，定义page变量
        HighlightPage<SolrItem> page = solrTemplate.queryForHighlightPage(query, SolrItem.class);
        //6.遍历解析page对象，page.getHighlighted().for，item = h.getEntity()，
        // item.setTitle(h.getHighlights().get(0).getSnipplets().get(0))，
        // 在设置高亮之前最好判断一下;
        for (HighlightEntry<SolrItem> h : page.getHighlighted()) {
            //先取出列表中的每一个结果
            SolrItem item = h.getEntity();
            //修改title，使用高亮
            if(h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0){
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }

        }
        //7.在循环完成外map.put("rows", page.getContent())返回数据列表
        //返回商品列表
        result.put("rows", page.getContent());
        //返回分页参数
        result.put("total",page.getTotalElements());
        result.put("totalPages",page.getTotalPages());
    }
}
