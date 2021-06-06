USE moviedb;

/* Add price to each movie */
ALTER TABLE movies ADD price INTEGER NOT NULL DEFAULT 1;
UPDATE movies SET price = FLOOR(1 + RAND() * (10 - 1 + 1));

/* Add quantity to each sale */
ALTER TABLE sales ADD quantity INTEGER NOT NULL DEFAULT 1;

/* Add an employee */
INSERT INTO employees 
VALUES ('classta@email.edu', 'classta', 'TA CS122B');
