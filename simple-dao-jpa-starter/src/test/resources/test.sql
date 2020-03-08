Select u.area , Avg( u.score ) , Sum( u.score )
From com.levin.commons.dao.domain.User u
Where u.area >  :? AND u.state LIKE  :?
Group By  u.area
Having  u.enable = :? AND ( NOT (u.name LIKE  :?)  OR  NOT (u.name =  :?) )

 Order By  u.area Desc