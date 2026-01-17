package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.hmdp.utils.SystemConstants;

import javax.servlet.http.HttpSession;
import java.util.Objects;

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

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        // 2.生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 3.保存验证码到 session 中
        session.setAttribute("code", code);

        // 4.发送验证码(模拟发送验证码)
        log.info("发送验证码成功，验证码为：{}", code);

        // 5.返回响应
        return Result.ok("验证码发送成功");
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String userCode = loginForm.getCode();
        String trueCode = (String)session.getAttribute("code");
        String phone = loginForm.getPhone();
        // 1.校验验证码是否合法
        if (!Objects.equals(userCode, trueCode)) {
            // 校验失败
            return Result.fail("验证码有误");
        }

        // 2.校验手机号是否合法
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }

        // 3.查询手机号是否存在
        User user = query().eq("phone", phone).one();
        if (user == null) {
            // 3.1 用户不存在，注册新用户
            user = new User();
            user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX+phone);
            user.setPhone(phone);
            save(user);
        }
        // 4.存储用户信息到 session 中
        session.setAttribute("user", user);
        return Result.ok();
    }
}
