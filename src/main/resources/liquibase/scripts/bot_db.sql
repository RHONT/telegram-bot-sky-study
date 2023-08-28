-- liquibase formatted sql

-- changeset Rhont:1
CREATE TABLE notification_task
(
    id      serial primary key,
    id_chat int,
    date    timestamp,
    message  varchar(50)
);