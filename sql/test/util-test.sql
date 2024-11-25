DROP PROCEDURE IF EXISTS verifyIfViewExists;
DROP PROCEDURE IF EXISTS verifyColumnNameAndType;
DROP PROCEDURE IF EXISTS verifyCountOfColumnsInTable;
DROP PROCEDURE IF EXISTS verifyIfStoredProcedureExists;

DELIMITER //

CREATE PROCEDURE verifyColumnNameAndType(IN _tableName VARCHAR(64), IN _columnName VARCHAR(64),
                                         IN _columnType VARCHAR(64))
BEGIN
    DECLARE column_type VARCHAR(64);
    DECLARE count INT;

    SELECT count(*)
    INTO count
    FROM information_schema.columns
    WHERE table_name = _tableName
      AND column_name = _columnName;

    IF count = 0 THEN
        SET @message = CONCAT('Column ', _tableName, '.', _columnName, ' does not exist');
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = @message;
    END IF;

    SELECT data_type
    INTO column_type
    FROM information_schema.columns
    WHERE table_name = _tableName
      AND column_name = _columnName;

    IF column_type != _columnType THEN
        SET @message = CONCAT('Column ', _tableName, '.', _columnName, ' is NOT ', _columnType);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = @message;
    END IF;
    SET @message = CONCAT('Column ', _tableName, '.', _columnName, ' is ', _columnType);
    SELECT @message;
END //

CREATE PROCEDURE verifyIfViewExists(IN _viewName VARCHAR(64))
BEGIN
    DECLARE viewCount INT;

    SELECT COUNT(*)
    INTO viewCount
    FROM information_schema.views
    WHERE table_name = _viewName;

    IF viewCount = 0 THEN
        SET @message = CONCAT('View ', _viewName, ' does not exist');
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = @message;
    END IF;
    SET @message = CONCAT('View ', _viewName, ' exists');
    SELECT @message;
END //

CREATE PROCEDURE verifyCountOfColumnsInTable(IN _tableName VARCHAR(64), IN _columnCount INT)
BEGIN
    DECLARE columnCount INT;

    SELECT COUNT(*)
    INTO columnCount
    FROM information_schema.columns
    WHERE table_name = _tableName;

    IF columnCount != _columnCount THEN
        SET @message = CONCAT('Table ', _tableName, ' has ', columnCount, ' columns');
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = @message;
    END IF;
    SET @message = CONCAT('Table ', _tableName, ' has ', columnCount, ' columns');
    SELECT @message;
END //

CREATE PROCEDURE verifyIfStoredProcedureExists(IN _procedureName VARCHAR(64))
BEGIN
    DECLARE procedureCount INT;

    SELECT COUNT(*)
    INTO procedureCount
    FROM information_schema.routines
    WHERE routine_name = _procedureName;

    IF procedureCount = 0 THEN
        SET @message = CONCAT('Procedure ', _procedureName, ' does not exist');
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = @message;
    END IF;
    SET @message = CONCAT('Procedure ', _procedureName, ' exists');
    SELECT @message;
END //

DELIMITER ;
