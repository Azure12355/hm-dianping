package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.hmdp.utils.SystemConstants;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        // 2.生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 保存验证码到 session 中
        // session.setAttribute("code", code);
        // 3.分布式session 改造：将验证码存储到 Redis 中, 保存的时间为 2min
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 4.发送验证码(模拟发送验证码)
        log.info("发送验证码成功，验证码为：{}", code);

        // 5.返回响应
        return Result.ok("验证码发送成功");
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {

        // 1.取出登录表单中的 code 和 phone
        String userCode = loginForm.getCode();
        String phone = loginForm.getPhone();

        // 2.校验手机号是否合法（校验手机最好放在校验验证码之前，减少查询数据库，效率高）
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }

        // 3.分布式改造：根据 login:code:phone 从 Redis 中取出真实的验证码
        String trueCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);

        // 4.校验验证码是否合法
        if (!Objects.equals(userCode, trueCode)) {
            // 4.1 校验失败， 直接返回
            return Result.fail("验证码有误");
        }

        // 5.查询手机号是否存在
        User user = query().eq("phone", phone).one();
        if (user == null) {
            // 5.1 用户不存在，注册新用户
            user = new User();
            user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX+phone);
            user.setPhone(phone);
            save(user);
        }

        // 6. 生成用户 token
        String token = UUID.randomUUID().toString();
        UserDTO userDTO = user.getUserDTO();
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

        // 7. 分布式改造：将用户信息存在 Redis 中
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

        // 8. 设置 token 的有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.ok(token);
    }
}
