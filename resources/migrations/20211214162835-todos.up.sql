CREATE TABLE todos (
    id SERIAL,
    label varchar,
    marked boolean DEFAULT false
);