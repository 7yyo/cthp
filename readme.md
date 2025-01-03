# CTHP

Help `MySQL protocol` databases that don't support the `create table as select.. From` syntax to automatically generate table schema.


## Example 1
### sql
```sql
create table tmp as 
    select t1.*, t2.id t2_id, 'hello' word 
    from t1 
    left join t2 on t1.id = t2.id;

mysql> desc t1;
+-------+---------------+------+-----+---------+-------+
| Field | Type          | Null | Key | Default | Extra |
+-------+---------------+------+-----+---------+-------+
| id    | int           | NO   | PRI | NULL    |       |
| c1    | int           | YES  |     | NULL    |       |
| c2    | varchar(11)   | YES  |     | NULL    |       |
| c3    | date          | YES  |     | NULL    |       |
| c4    | decimal(10,2) | YES  |     | NULL    |       |
+-------+---------------+------+-----+---------+-------+
5 rows in set (0.02 sec)

mysql> desc t2;
+-------+----------+------+-----+---------+-------+
| Field | Type     | Null | Key | Default | Extra |
+-------+----------+------+-----+---------+-------+
| id    | int      | NO   | PRI | NULL    |       |
| c1    | datetime | YES  |     | NULL    |       |
+-------+----------+------+-----+---------+-------+
2 rows in set (0.01 sec)

CREATE TABLE tmp (
                     id INT,
                     c1 INT,
                     c2 VARCHAR(11),
                     c3 DATE,
                     c4 DECIMAL(10,2),
                     t2_id INT,
                     word VARCHAR(5)
);
```

## Example 2
```sql
create table t01
as
select
    basecurrency ,
    exchrate ,
    exchdate,
    case when begin_exchdate is null then '1900-01-01' else date_add(exchdate, interval 1 day) end begin_exchdate,
    ifnull(end_exchdate,'2030-01-01') end_exchdate
from (
         select basecurrency ,exchrate , exchdate
              ,lag(exchdate )over(partition by basecurrency ,exchcurrency  order by exchdate ) begin_exchdate
              ,lead(exchdate)over(partition by basecurrency ,exchcurrency  order by exchdate )  end_exchdate
         from test.t
         where exchcurrency ='CNY'
     ) tmp;

mysql> desc t;
+-------------------+---------------+------+-----+---------+-------+
| Field             | Type          | Null | Key | Default | Extra |
+-------------------+---------------+------+-----+---------+-------+
| exchdate          | date          | YES  | MUL | NULL    |       |
| base              | decimal(12,0) | YES  |     | NULL    |       |
| basecurrency      | varchar(3)    | YES  |     | NULL    |       |
| exchcurrency      | varchar(3)    | YES  |     | NULL    |       |
| exchrate          | decimal(10,4) | YES  |     | NULL    |       |
| buyprice          | decimal(10,4) | YES  |     | NULL    |       |
| saleprice         | decimal(10,4) | YES  |     | NULL    |       |
| cashprice         | decimal(10,4) | YES  |     | NULL    |       |
| flag              | varchar(2)    | YES  |     | NULL    |       |
| validstatus       | varchar(1)    | YES  |     | NULL    |       |
| inserttimeforhis  | varchar(25)   | YES  |     | NULL    |       |
| operatetimeforhis | varchar(25)   | YES  |     | NULL    |       |
+-------------------+---------------+------+-----+---------+-------+
12 rows in set (0.04 sec)

CREATE TABLE t01 (
                     basecurrency VARCHAR(3),
                     exchrate DECIMAL(10,4),
                     exchdate DATE,
                     begin_exchdate VARCHAR(10),
                     end_exchdate VARCHAR(10)
);
```