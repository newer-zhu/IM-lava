package com.github.yuanrw.im.rest.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yuanrw.im.common.domain.po.User;
import com.github.yuanrw.im.rest.web.mapper.UserMapper;
import com.github.yuanrw.im.rest.web.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

/**
 * default user service implement
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User verifyAndGet(String username, String pwd) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (null != user){
            return verityPassword(pwd, user.getSalt(), user.getPwdHash()) ? user : null;
        }
        return null;
    }

    private boolean verityPassword(String pwd, String salt, String pwdHash) {
        String hashRes = DigestUtils.sha256Hex(pwd + salt);
        return hashRes.equals(pwdHash);
    }
}
