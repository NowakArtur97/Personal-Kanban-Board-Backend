CREATE TABLE personal_kanban_board.tasks (
"task_id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "title" VARCHAR(100) NOT NULL,
    "description" VARCHAR(100),
    "status" VARCHAR(50) NOT NULL,
    "priority" VARCHAR(20) NOT NULL,
    "target_end_date" DATE,
    "assigned_to" UUID NOT NULL,
    "created_by" UUID NOT NULL,
    "created_on" DATE NOT NULL,
    "updated_by" UUID,
    "updated_on" DATE,

    CONSTRAINT "fk_tasks_assigned_to" FOREIGN KEY ("assigned_to") REFERENCES personal_kanban_board.users("user_id") ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT "fk_tasks_created_by" FOREIGN KEY ("created_by") REFERENCES personal_kanban_board.users("user_id") ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT "fk_tasks_updated_by" FOREIGN KEY ("updated_by") REFERENCES personal_kanban_board.users("user_id") ON DELETE NO ACTION ON UPDATE CASCADE
);
