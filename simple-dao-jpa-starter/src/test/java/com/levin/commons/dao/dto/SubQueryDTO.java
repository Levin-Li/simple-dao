package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.DefaultPaging;

import java.util.Date;


@TargetOption(entityClass = User.class, maxResults = 100)
public class SubQueryDTO {

    Paging paging = new DefaultPaging(1, 20);


    Long id;

    @OrderBy
    String name = "User";


    @In()
    String[] state = new String[]{"A", "B", "C"};

    @AND
    protected Boolean editable = true;

    @Lt(fieldFuncs = @Func(value = "DATE_FORMAT", params = {"$$", "${:_this.format}"}),paramExpr = "select createTime from "+E_User.CLASS_NAME+" ")
    @OR
    protected Date createTime = new Date();

    @Between("score")
    @END
    protected Integer[] scores = new Integer[]{200, 100, null, null};

    @Like
    @END
    protected String description = "";

    @Ignore
    String format = "YYYY-MM-DD";

    //  From com.levin.commons.dao.domain.User u   Where u.name =  :? AND u.state IN ( :?,:?,:? ) AND u.editable =  :? AND (DATE_FORMAT(u.createTime , :?) <  :? OR u.score Between  :?AND:?)  Order By  u.name Desc

}
