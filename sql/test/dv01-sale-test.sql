CALL verifyIfViewExists('dv01_saleline');

CALL verifyCountOfColumnsInTable('dv01_saleline', 7);

CALL verifyColumnNameAndType('dv01_saleline', 'id', 'int');
CALL verifyColumnNameAndType('dv01_saleline', 'product', 'varchar');
CALL verifyColumnNameAndType('dv01_saleline', 'size', 'varchar');
CALL verifyColumnNameAndType('dv01_saleline', 'quantity', 'int');
CALL verifyColumnNameAndType('dv01_saleline', 'unitPrice', 'decimal');
CALL verifyColumnNameAndType('dv01_saleline', 'options', 'text');
CALL verifyColumnNameAndType('dv01_saleline', 'id_dv01_sale', 'int');
