package com.levin.commons.dao.service;

import com.levin.commons.dao.service.dto.QueryUserEvt;
import com.levin.commons.dao.service.dto.UserInfo;

import java.util.List;

public interface UserService {



    List<UserInfo> findUserInfo(QueryUserEvt evt);



}
