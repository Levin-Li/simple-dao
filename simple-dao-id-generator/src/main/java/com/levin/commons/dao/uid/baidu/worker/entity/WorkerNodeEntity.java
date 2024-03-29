package com.levin.commons.dao.uid.baidu.worker.entity;

import com.levin.commons.dao.uid.baidu.worker.WorkerNodeType;
import com.levin.commons.service.domain.Identifiable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.Date;

//1、lobmok get set
@Data

//2、必须注解主键字段
@EqualsAndHashCode(of = {"id"})

//3、必须使用链式设置
@Accessors(chain = true)

//4、必须生成常量字段
@FieldNameConstants

//5、必须注解业务名称
@Schema(description = "工作节点")

//6、必须建立索引
@Entity

//7、必须建立索引
@Table(
        name = "uuid_worker_node_entity",
        indexes = {
                @Index(columnList = "host_name"),
                @Index(columnList = "port"),
                @Index(columnList = "type"),
        }
        ,
        uniqueConstraints = {
//                @UniqueConstraint(columnNames = {})
        }
)

public class WorkerNodeEntity implements Identifiable {

    @Schema(description = "ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * Type of CONTAINER: HostName, ACTUAL : IP.
     */
    @Column(name = "host_name")
    String hostName;

    /**
     * Type of CONTAINER: Port, ACTUAL : Timestamp + Random(0-10000)
     */
    String port;

    /**
     * type of {@link WorkerNodeType}
     */
    int type;

    /**
     * Worker launch date, default now
     */
    @Column(name = "launch_date")
    Date launchDate = new Date();

    /**
     * Created time
     */
    Date created;

    /**
     * Last modified
     */
    Date modified;

    @PrePersist
    public void prePersist() {
        if (created == null) {
            created = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (modified == null) {
            modified = new Date();
        }
    }

}
