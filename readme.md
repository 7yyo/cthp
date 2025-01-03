# CTHP

Help `MySQL protocol` databases that don't support the `create table as select.. From` syntax to automatically generate table schema.


## Example
### sql
```sql
create table tmp as 
    select t1.*, t2.id t2_id, 'hello' word 
    from t1 
    left join t2 on t1.id = t2.id;
```
### schema
```sql
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
```
### generated schema
```sql
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