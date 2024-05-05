ALTER TABLE personal_kanban_board.users ADD COLUMN role VARCHAR(50) NOT NULL;
ALTER TABLE personal_kanban_board.users ADD CONSTRAINT "check_users_role" CHECK (role IN ('USER', 'ADMIN'));
