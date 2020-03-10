Select
Min( score ) ,
Max( score ) ,
Avg( score ) ,
Count( 1 ) ,
state
From com.levin.commons.dao.domain.support.TestEntity
Where
state NOT IN (  ?1 , ?2 , ?3  )
AND name LIKE '%'||  ?4  ||'%'
Group By  state