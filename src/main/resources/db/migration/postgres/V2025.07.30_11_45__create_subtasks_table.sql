CREATE TABLE personal_kanban_board.subtasks (
    "subtask_id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "task_id" UUID NOT NULL,
    "title" VARCHAR(100) NOT NULL,
    "description" VARCHAR(1000),
    "status" VARCHAR(50) NOT NULL,
    "priority" VARCHAR(20) NOT NULL,
    "target_end_date" DATE,
    "assigned_to" UUID NOT NULL,
    "created_by" UUID NOT NULL,
    "created_on" TIMESTAMP NOT NULL,
    "updated_by" UUID,
    "updated_on" TIMESTAMP,

    CONSTRAINT "fk_subtasks_task_id" FOREIGN KEY ("task_id") REFERENCES personal_kanban_board.tasks("task_id") ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT "fk_subtasks_assigned_to" FOREIGN KEY ("assigned_to") REFERENCES personal_kanban_board.users("user_id") ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT "fk_subtasks_created_by" FOREIGN KEY ("created_by") REFERENCES personal_kanban_board.users("user_id") ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT "fk_subtasks_updated_by" FOREIGN KEY ("updated_by") REFERENCES personal_kanban_board.users("user_id") ON DELETE NO ACTION ON UPDATE CASCADE
);
