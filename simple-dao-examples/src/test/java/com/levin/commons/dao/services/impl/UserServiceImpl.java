package com.levin.commons.dao.services.impl;

import com.levin.commons.dao.JpaDao;
import com.levin.commons.dao.services.UserService;
import com.levin.commons.dao.services.dto.QueryUserEvt;
import com.levin.commons.dao.services.dto.UserInfo;
import com.levin.commons.dao.services.dto.UserUpdateEvt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    JpaDao jpaDao;


    @Override
    public List<UserInfo> findUserInfo(QueryUserEvt evt) {

        return jpaDao.findByQueryObj(UserInfo.class, evt);

    }


    @Override
    public boolean addUserScore(UserUpdateEvt evt) {
        return jpaDao.updateByQueryObj(evt) > 0;
    }

}
