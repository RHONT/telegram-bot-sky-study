-- liquibase formatted sql

-- changeset Rhont:1
CREATE TABLE notification_task
(
    id      serial primary key,
    id_chat int,
    date    timestamp,
    message  varchar(50)
);

-- liquibase formatted sql

-- changeset Rhont:2
CREATE INDEX date_index ON notification_task (date);