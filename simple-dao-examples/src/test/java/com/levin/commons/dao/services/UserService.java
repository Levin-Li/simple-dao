package com.levin.commons.dao.services;

import com.levin.commons.dao.services.dto.QueryUserEvt;
import com.levin.commons.dao.services.dto.UserInfo;
import com.levin.commons.dao.services.dto.UserUpdateEvt;

import java.util.List;

public interface UserService {


    List<UserInfo> findUserInfo(QueryUserEvt evt);


    boolean addUserScore(UserUpdateEvt evt);

}
