package org.seckill.servie.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.servie.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/14.
 */
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SeckillDao seckillDao;
    private SuccessKilledDao successKilledDao;
    private final String slat = "gHGHJHGhas15326136@%%$^%$##)jafhja%#$+|+|";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = seckillDao.queryById(seckillId);
        if (seckill == null) {
            return new Exposer(false, seckillId);
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        //转化字符串过程,不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);

    }
    //md5加密
    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    public SeckillExecution executeSeckill(long seckillId,  long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if(md5==null||md5.equals(getMD5(seckillId))){
            throw  new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime=new Date();
        try {
            //减库存
            int updateCount=seckillDao.reduceNumber(seckillId,nowTime);
            if (updateCount<=0){
                //没有更新到记录，秒杀结束
                throw new SeckillCloseException("seckill is closed");
            }else{
               //记录购买行为
                int insertCount=successKilledDao.insertSuccessKilled(seckillId,userPhone);
                //重复秒杀
                if (insertCount<=0){
                    //重复秒杀
                    throw new RepeatKillException("seckill repeated");
                }else{
                     //秒杀成功
                    SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,successKilled);
                }

            }
        }catch (SeckillCloseException e1){
            throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        } catch (Exception e){
           logger.error(e.getMessage(),e);
           throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }
}
