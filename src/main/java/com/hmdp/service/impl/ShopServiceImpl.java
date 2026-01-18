package com.hmdp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        // 1.查询 Redis 中是否存在
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);

        // 2.如果 Redis 缓存命中则直接放回
        if (ObjectUtil.isNotEmpty(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }

        // 3.如果 Redis 缓存未命中则查询数据库
        Shop shop = getById(id);

        // 4.如果数据库未命中则直接返回 404 报错
        if (ObjectUtil.isEmpty(shop)) {
            return Result.fail("查询的商户 id 有误");
        }

        // 5.如果数据库命中则写入数据到 Redis 中并返回
        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop));

        return Result.ok(shop);
    }
}
