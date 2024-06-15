CREATE TABLE PERSONAL_KANBAN_BOARD.TASKS (
    "TASK_ID" UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    "TITLE" VARCHAR(100) NOT NULL,
    "DESCRIPTION" VARCHAR(100),
    "STATUS" VARCHAR(50) NOT NULL,
    "PRIORITY" VARCHAR(20) NOT NULL,
    "TARGET_END_DATE" DATE,
    "ASSIGNED_TO" UUID NOT NULL,
    "CREATED_BY" UUID NOT NULL,
    "CREATED_ON" DATE NOT NULL,
    "UPDATED_BY" UUID,
    "UPDATED_ON" DATE,

    CONSTRAINT "fk_tasks_assigned_to" FOREIGN KEY ("ASSIGNED_TO") REFERENCES PERSONAL_KANBAN_BOARD.USERS("USER_ID") ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT "fk_tasks_created_by" FOREIGN KEY ("CREATED_BY") REFERENCES PERSONAL_KANBAN_BOARD.USERS("USER_ID") ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT "fk_tasks_updated_by" FOREIGN KEY ("UPDATED_BY") REFERENCES PERSONAL_KANBAN_BOARD.USERS("USER_ID") ON DELETE NO ACTION ON UPDATE CASCADE
);
