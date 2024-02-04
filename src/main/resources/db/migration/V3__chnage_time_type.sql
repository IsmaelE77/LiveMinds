-- Alter the table to change the data type of the time column to TIMESTAMP WITH TIME ZONE
ALTER TABLE room
ALTER COLUMN time SET DATA TYPE TIMESTAMP WITH TIME ZONE;
