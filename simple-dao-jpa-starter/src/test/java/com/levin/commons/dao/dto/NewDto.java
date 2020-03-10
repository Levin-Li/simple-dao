package com.levin.commons.dao.dto;

import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.CList;
import com.levin.commons.dao.annotation.Op;

public class NewDto {

    @C
    @CList({@C(op = Op.StartsWith)})
    String name;


    @C(op = Op.Gt)
    Integer age;

    @CList({@C(op = Op.StartsWith)})
    String detail;


    //不加任何注解
    CommDto commDto = new CommDto();

}
