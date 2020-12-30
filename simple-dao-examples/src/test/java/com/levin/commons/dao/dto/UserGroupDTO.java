package com.levin.commons.dao.dto;

import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.User;

/**
 * Created by echo on 2017/4/30.
 */
public class UserGroupDTO {

    private User user;
    private Group group;


    public UserGroupDTO(User user, Group group) {
        this.user = user;
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public Group getGroup() {
        return group;
    }
}
