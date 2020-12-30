package com.levin.commons.dao.service.impl;

import com.levin.commons.dao.JpaDao;
import com.levin.commons.dao.service.UserService;
import com.levin.commons.dao.service.dto.QueryUserEvt;
import com.levin.commons.dao.service.dto.UserInfo;
import com.levin.commons.dao.service.dto.UserUpdateEvt;
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
