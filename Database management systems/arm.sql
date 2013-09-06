create or replace
PROCEDURE cleanup authid current_user
as
VCOUNT number;
TNAME VARCHAR(20);
begin
TNAME := ''''||'ITEMS'||'''';
EXECUTE immediate 'Select count(1) from all_tables where table_name ='||TNAME INTO vcount;
  DBMS_OUTPUT.PUT_LINE(VCOUNT);
  if(VCOUNT=1) then
  execute immediate 'DROP TABLE ITEMS';
  end if;
  execute immediate  'create table items(itemid number(4) ,itemname varchar2(100),primary key(itemid))';
  
  TNAME := ''''||'TRANS'||'''';
EXECUTE immediate 'Select count(1) from all_tables where table_name ='||TNAME INTO vcount;
  DBMS_OUTPUT.PUT_LINE(VCOUNT);
  if(VCOUNT=1) then
  execute immediate 'DROP TABLE TRANS';
  end if;
  EXECUTE IMMEDIATE  'create table trans(transid number(4) ,itemid number(4),primary key(transid,itemid))';
  
END;
/


CREATE OR REPLACE
PROCEDURE CANDIDATE_SETS(
    FI_SIZE IN NUMBER)
IS
  I           NUMBER;
  SELECT_STMT varchar2(5000);
  FROM_STMT   varchar2(5000);
  WHERE_STMT  varchar2(5000);
  ORDER_STMT  varchar2(5000);
  SQL_SEL     varchar2(5000);
  SQL_ins     VARCHAR2(5000);
begin
  I   := 1;
  SELECT_STMT := 'SELECT ';
  FROM_STMT   := ' from fiset_'||TO_CHAR(FI_SIZE-1)||' a, '||'fiset_'||TO_CHAR(FI_SIZE-1)||' b';
  WHERE_STMT := ' WHERE ';
  ORDER_STMT := ' ORDER BY ';
  --DBMS_OUTPUT.PUT_LINE(FROM_STMT);
  WHILE(I<FI_SIZE)
  LOOP
    --DBMS_OUTPUT.PUT_LINE(I);
    SELECT_STMT  := SELECT_STMT || 'a.itemid'||I||', ';
    IF(I          <(FI_SIZE-1)) THEN
      WHERE_STMT :=WHERE_STMT||'a.itemid'||TO_CHAR(I)||' = '||'b.itemid'||I||' AND ';
      ORDER_STMT :=ORDER_STMT||'a.itemid'||TO_CHAR(I)||' , ';
    ELSE
      WHERE_STMT :=WHERE_STMT||'a.itemid'||TO_CHAR(I)||' < '||'b.itemid'||I||' ';
      ORDER_STMT :=ORDER_STMT||'a.itemid'||TO_CHAR(I)||' ';
    END IF;
    I :=I+1;
  END LOOP;
  SQL_SEL :=SELECT_STMT||'b.itemid'||TO_CHAR(FI_SIZE-1)||' '||FROM_STMT || ' '||WHERE_STMT||' '|| ORDER_STMT ;
  SQL_INS := 'INSERT INTO cset_'||TO_CHAR(FI_SIZE)||' '||SQL_SEL||' ';
  --DBMS_OUTPUT.PUT_LINE(SQL_SEL);
  --DBMS_OUTPUT.PUT_LINE(SQL_INS);
  execute immediate SQL_INS;
END;

/

CREATE OR REPLACE
PROCEDURE FREQUENT_ITEM_SETS(
    FI_SIZE IN NUMBER,
    Support IN NUMBER)
IS
  I           number;
  SELECT_STMT VARCHAR2(5000);
  FROM_STMT   varchar2(5000);
  WHERE_STMT  varchar2(5000);
  ORDER_STMT  varchar2(5000);
  GRPBY_STMT  varchar2(5000);
  HAVING_STMT varchar2(5000);
  SQL_SEL     varchar2(5000);
  SQL_ins     VARCHAR2(5000);
BEGIN
  I := 1;
  --IF (I=1) THEN
  --DBMS_OUTPUT.PUT_LINE(I);
  --END IF;
  SELECT_STMT := ' SELECT ';
  FROM_STMT   := ' FROM ';
  WHERE_STMT  := ' WHERE ';
  ORDER_STMT  := ' ORDER BY ';
  GRPBY_STMT  := ' GROUP BY ';
  WHILE(I     <=FI_SIZE)
  LOOP
    --DBMS_OUTPUT.PUT_LINE(I);
    SELECT_STMT:=SELECT_STMT||'itemid'||TO_CHAR(I)||', ';
    FROM_STMT  := FROM_STMT||'TRANS'||' t'||TO_CHAR(I)||', ';
    IF(I       <=(FI_SIZE-1)) THEN
      -- DBMS_OUTPUT.PUT_LINE('test');
      WHERE_STMT := WHERE_STMT||'t'||TO_CHAR(I)||'.itemid = '||'Cset_'||TO_CHAR(FI_SIZE)||'.itemid'||TO_CHAR(I)||' AND '||'t'||TO_CHAR(I)||'.transid = '||'t'||TO_CHAR(I+1)||'.transid AND ';
      GRPBY_STMT :=GRPBY_STMT ||'itemid'||TO_CHAR(I)||' , ';
    ELSE
      WHERE_STMT := WHERE_STMT||'t'||i||'.itemid = '||'Cset_'||TO_CHAR(FI_SIZE)||'.itemid'||i||' ';
      GRPBY_STMT :=GRPBY_STMT||'itemid'||TO_CHAR(I);
    END IF;
    I :=I+1;
  END LOOP;
  SELECT_STMT := SELECT_STMT ||'count(*) ';
  FROM_STMT   :=FROM_STMT||' '||'CSET_'||TO_CHAR(FI_SIZE);
  HAVING_STMT := ' HAVING count (*) >='||SUPPORT;
  --DBMS_OUTPUT.PUT_LINE(SELECT_STMT);
  --DBMS_OUTPUT.PUT_LINE(FROM_STMT);
  --DBMS_OUTPUT.PUT_LINE(WHERE_STMT);
  --DBMS_OUTPUT.PUT_LINE(GRPBY_STMT);
  --DBMS_OUTPUT.PUT_LINE(HAVING_STMT);
  SQL_SEL := SELECT_STMT||FROM_STMT||WHERE_STMT||GRPBY_STMT||HAVING_STMT;
  --DBMS_OUTPUT.PUT_LINE(SQL_SEL);
  SQL_INS := 'INSERT INTO '||'FISET_'||FI_SIZE||SQL_SEL;
  --DBMS_OUTPUT.PUT_LINE(SQL_INS);
  EXECUTE immediate SQL_INS;
END;

/
exit;



