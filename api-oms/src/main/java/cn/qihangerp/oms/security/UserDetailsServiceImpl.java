package cn.qihangerp.oms.security;

import cn.qihangerp.common.enums.UserStatus;
import cn.qihangerp.common.exception.ServiceException;
import cn.qihangerp.common.utils.MessageUtils;
import cn.qihangerp.common.utils.StringUtils;
import cn.qihangerp.oms.domain.ScmDistributor;
import cn.qihangerp.oms.service.ScmDistributorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户验证处理
 *
 * @author qihang
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ScmDistributorService userService;
    @Autowired
    private UserPasswordService passwordService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        ScmDistributor user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
        else if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }

        passwordService.validate(user);

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(ScmDistributor user)
    {
        Set<String> perms = new HashSet<String>();
        perms.add("*:*:*");
//        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
        return new LoginDistributor(user.getUserId(), user, perms);
    }
}
