package com.hmdp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryShopTypeList() {
        // 1.查询商铺类型缓存
        String shopTypeListJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_TYPE_KEY);

        // 2.缓存命中，直接返回
        List<ShopType> shopTypeList = JSONUtil.toList(shopTypeListJson, ShopType.class);
        if (ObjectUtil.isNotEmpty(shopTypeList)) {
            return Result.ok(shopTypeList);
        }
        // 3.缓存未命中，查询数据库
        shopTypeList = list();

        // 5.数据库命中，写入到 Redis 中，然后返回数据
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopTypeList));

        return Result.ok(shopTypeList);
    }
}
