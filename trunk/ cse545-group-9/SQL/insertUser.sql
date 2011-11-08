INSERT INTO mydb.Users (uname,fname,lname,email,role,dept,pwd) VALUES ('alice','Alice','Smith','alice@mail.com',4,'HR,FN',md5('@@459owH500o4'));
INSERT INTO mydb.Users (uname,fname,lname,email,role,dept,pwd) VALUES ('bob','Bob','Smith','bob@mail.com',3,'FN',md5('@@e07p4j6Y544j'));
INSERT INTO mydb.Users (uname,fname,lname,email,role,dept,pwd) VALUES ('mike','Mike','Jones','mike@mail.com',2,'HR',md5('%Qwerty1234'));
INSERT INTO mydb.Users (uname,fname,lname,email,role,dept,pwd) VALUES ('phil','Phil','Anderson','phil@mail.com',1,'GUEST',md5('#Qwerty1234'));
INSERT INTO mydb.Users (uname,fname,lname,email,role,dept,pwd) VALUES ('sara','Sara','Smith','sara@mail.com',0,'TEMP',md5('#Qwerty1234'));

INSERT INTO mydb.Groups (groupid,uname) VALUES ('appuser','alice');
INSERT INTO mydb.Groups (groupid,uname) VALUES ('appuser','bob');
INSERT INTO mydb.Groups (groupid,uname) VALUES ('appadmin','mike');
INSERT INTO mydb.Groups (groupid,uname) VALUES ('appuser','phil');