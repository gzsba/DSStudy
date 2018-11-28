package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		
		PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbContent> list = contentMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());
		return result;
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insertSelective(content);
		//清除相同类型的缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	/*public static void main(String[] args) {
		Long a = 128L;
		Long b = 128L;
		System.out.println(a.longValue() == b.longValue());
	}*/

	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//必须在修改之前，查询出之前的广告分类
		TbContent beUpdate = contentMapper.selectByPrimaryKey(content.getId());
		if(beUpdate.getCategoryId().longValue() != content.getCategoryId().longValue()){
			redisTemplate.boundHashOps("content").delete(beUpdate.getCategoryId());
		}

		contentMapper.updateByPrimaryKeySelective(content);
		//清除缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //在删除之前先查询所有的分类
		List<TbContent> contents = contentMapper.selectByExample(example);
		for (TbContent content : contents) {
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}

		//跟据查询条件删除数据
        contentMapper.deleteByExample(example);
	}
	
	
	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						//如果字段不为空
			if (content.getTitle()!=null && content.getTitle().length()>0) {
				criteria.andLike("title", "%" + content.getTitle() + "%");
			}
			//如果字段不为空
			if (content.getUrl()!=null && content.getUrl().length()>0) {
				criteria.andLike("url", "%" + content.getUrl() + "%");
			}
			//如果字段不为空
			if (content.getPic()!=null && content.getPic().length()>0) {
				criteria.andLike("pic", "%" + content.getPic() + "%");
			}
			//如果字段不为空
			if (content.getStatus()!=null && content.getStatus().length()>0) {
				criteria.andLike("status", "%" + content.getStatus() + "%");
			}
	
		}

        //查询数据
        List<TbContent> list = contentMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());
		
		return result;
	}

    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
    	//先查询缓存
		List<TbContent> contents = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
		//缓存没，查询数据库，设置进去
		if (contents == null || contents.size() < 1) {
			Example example = new Example(TbContent.class);
			Example.Criteria criteria = example.createCriteria();
			//组装查询条件
			criteria.andEqualTo("categoryId", categoryId);
			//排序，语法：属性名 asc|desc，多个字段以逗号分隔
			example.setOrderByClause("sortOrder asc");
			contents = contentMapper.selectByExample(example);

			//把数据放入缓存
			redisTemplate.boundHashOps("content").put(categoryId, contents);
		}else{
			System.out.println("从缓存中加载了广告内容...");
		}

		return contents;
    }

}
