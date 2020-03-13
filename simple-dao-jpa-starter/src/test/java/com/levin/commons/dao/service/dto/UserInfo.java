package com.levin.commons.dao.service.dto;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserInfo {

    String id;

    String name;

    String state;

    String area;

}
