/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.levin.commons.dao.uid.baidu.worker;

import com.levin.commons.dao.uid.baidu.ModuleOption;
import com.levin.commons.dao.uid.baidu.utils.DockerUtils;
import com.levin.commons.dao.uid.baidu.utils.NetUtils;
import com.levin.commons.dao.uid.baidu.worker.dao.WorkerNodeDAO;
import com.levin.commons.dao.uid.baidu.worker.entity.WorkerNodeEntity;
import com.levin.commons.utils.MapUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Represents an implementation of {@link WorkerIdAssigner},
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author yutianbao
 */
@Service(ModuleOption.PLUGIN_PREFIX + "WorkerIdAssigner")
public class DisposableWorkerIdAssigner
        implements WorkerIdAssigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisposableWorkerIdAssigner.class);

    @Autowired(required = false)
    WorkerNodeDAO workerNodeDAO;

    @Autowired(required = false)
    JdbcOperations jdbcOperations;

    @Autowired(required = false)
    SimpleJdbcInsert jdbcInsert;

    /**
     * Assign worker id base on database.<p>
     * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
     * Otherwise, the node runs on an actual machine.
     *
     * @return assigned worker id
     */
    @Transactional
    @Override
    public long assignWorkerId() {

        // build worker node entity
        WorkerNodeEntity node1 = buildWorkerNode();

        // add worker node for new (ignore the same IP + PORT)
        // workerNodeDAO.save(node1);
        if (workerNodeDAO != null) {

            WorkerNodeEntity node = workerNodeDAO.findByHostNameAndPort(node1.getHostName(), node1.getPort());

            if (node != null) {
                node1 = node;
            } else {
                workerNodeDAO.save(node1);
            }

        } else if (jdbcOperations != null && jdbcInsert!=null) {

            Map<String, Object> node = findNode(node1);

            if (node != null && !node.isEmpty()) {
                node1.setId((Long) node.get("id"));
            } else {

                node1.prePersist();

                jdbcInsert.withTableName("uuid_worker_node_entity")
                        .usingGeneratedKeyColumns("id")
                        .execute(MapUtils.putFirst("host_name", node1.getHostName())
                                .put("port", node1.getPort())
                                .put("type", node1.getType())
                                .put("launch_date", node1.getLaunchDate())
                                .put("created", node1.getCreated())
                                .build());

                node = findNode(node1);

                node1.setId((Long) node.get("id"));
            }
        }

        if (node1.getId() == null) {
            node1.setId(System.currentTimeMillis());
        }

        return node1.getId();
    }

    private Map<String, Object> findNode(WorkerNodeEntity node1) {
        List<Map<String, Object>> mapList = jdbcOperations.queryForList("select id from uuid_worker_node_entity where host_name = ? and port = ? ", node1.getHostName(), node1.getPort());
        return (mapList != null && !mapList.isEmpty()) ? mapList.get(0) : null;
    }

    /**
     * Build worker node entity by IP and PORT
     */
    private WorkerNodeEntity buildWorkerNode() {

        WorkerNodeEntity workerNodeEntity = new WorkerNodeEntity();

        if (DockerUtils.isDocker()) {
            workerNodeEntity.setType(WorkerNodeType.CONTAINER.value());
            workerNodeEntity.setHostName(DockerUtils.getDockerHost());
            workerNodeEntity.setPort(DockerUtils.getDockerPort());

        } else {
            workerNodeEntity.setType(WorkerNodeType.ACTUAL.value());
            workerNodeEntity.setHostName(NetUtils.getLocalAddress());
            workerNodeEntity.setPort(System.currentTimeMillis() + "-" + RandomUtils.nextInt(100000));
        }

        return workerNodeEntity;
    }

}
