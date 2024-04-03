CREATE TABLE personal_kanban_board.tasks (
    "id" SERIAL PRIMARY KEY,
    "title" VARCHAR(100) NOT NULL,
    "description" VARCHAR(100)
)
