package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.utils.IdWorker;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service(timeout = 5000)
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insertSelective(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKeySelective(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        seckillOrderMapper.deleteByExample(example);
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            //如果字段不为空
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andLike("userId", "%" + seckillOrder.getUserId() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andLike("sellerId", "%" + seckillOrder.getSellerId() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andLike("status", "%" + seckillOrder.getStatus() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andLike("receiverAddress", "%" + seckillOrder.getReceiverAddress() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andLike("receiverMobile", "%" + seckillOrder.getReceiverMobile() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andLike("receiver", "%" + seckillOrder.getReceiver() + "%");
            }
            //如果字段不为空
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andLike("transactionId", "%" + seckillOrder.getTransactionId() + "%");
            }

        }

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;

    @Override
    //方法加锁
    public synchronized void submitOrder(final Long seckillId, final String userId) {
        final TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        if (seckillGoods == null) {
            throw new RuntimeException("商品信息不存在！");
        }
        if (seckillGoods.getStockCount() < 1) {
            throw new RuntimeException("抱歉，你的手速慢了一些，商品已被抢购一空！");
        }
        //预占库存
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);

        //如果商品已被抢完，把商品数据同步回数据库
        if (seckillGoods.getStockCount() == 0) {
            //把商品信息清出缓存
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
            //开启新线程，去完成数据库操作
            new Thread() {
                @Override
                public void run() {
                    //把商品同步回数据库
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                    super.run();
                }
            }.start();

        }

        //保存订单到redis
        //在支付之前，保存订单到redis
        long orderId = idWorker.nextId();
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(orderId);
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
        seckillOrder.setSeckillId(seckillId);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setUserId(userId);//设置用户ID
        seckillOrder.setStatus("0");//状态-未付款状态
        //订单信息存入redis
        redisTemplate.boundHashOps("seckillOrders").put(userId, seckillOrder);
        /*new Thread() {
			@Override
			public void run() {

				super.run();
			}
		}.start();*/
    }

    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrders").get(userId);
    }

    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrders").get(userId);
        if (seckillOrder == null) {
            throw new RuntimeException("订单信息不存在!");
        }
        //用户正在支付的订单，不是同一个
        if (!seckillOrder.getId().equals(orderId)) {
            throw new RuntimeException("支付订单信息不匹配!");
        }

        //更新订单状态
        seckillOrder.setStatus("1");  //已支付
        seckillOrder.setPayTime(new Date());  //支付时间
        seckillOrder.setTransactionId(transactionId);  //微信交易流水号

        //把订单保存数据库
        seckillOrderMapper.insertSelective(seckillOrder);

        //清除订单缓存信息
        redisTemplate.boundHashOps("seckillOrders").delete(userId);
    }

    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder != null && orderId.longValue() == seckillOrder.getId().longValue()) {
            redisTemplate.boundHashOps("seckillOrder").delete(userId);//删除缓存中的订单

            //恢复库存
            //1.从缓存中提取秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
            seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            //恢复库存
            redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);

        }

    }
}