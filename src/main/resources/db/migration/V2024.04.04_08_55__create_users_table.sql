CREATE TABLE personal_kanban_board.users (
    "user_id" SERIAL PRIMARY KEY,
    "username" VARCHAR(100) NOT NULL UNIQUE,
    "password" VARCHAR(100) NOT NULL,
    "email" VARCHAR(100) NOT NULL UNIQUE,

    CONSTRAINT "check_users_email" CHECK (email ~* '^[A-Za-z0-9._+%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$')
);
