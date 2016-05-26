package com.aw.common.task;

import com.aw.common.cluster.Member;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A task container executes tasks within the task service.
 *
 *
 *
 */
@JsonDeserialize(as=RemoteTaskContainer.class)
public interface TaskContainer extends Member, TaskExecutor  {

}
