CREATE FUNCTION toEpoch ( input text)
RETURNS text LANGUAGE java
BODY
  return text.length < 3
    ? null
    : text
END BODY;


CREATE OR REPLACE FUNCTION testFun (input int) CALLED ON NULL INPUT RETURNS int LANGUAGE java AS 'return input*10';