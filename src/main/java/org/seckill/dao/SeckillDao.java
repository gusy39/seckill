package org.seckill.dao;

import org.seckill.entity.Seckill;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/12.
 */
public interface SeckillDao {
    /**
     *减库存
     * @param seckillId
     * @param killTime
     * @return
     */
    int reduceNumber(long seckillId, Date killTime);

    /**
     *根据id查询秒杀对象
     * @param seckillId
     * @return
     */
    Seckill queryById(long seckillId);

    /**
     *根据偏移量查询秒杀商品表
     * @param offet
     * @param limit
     * @return
     */
    List<Seckill> queryAll(int offet, int limit);
}
