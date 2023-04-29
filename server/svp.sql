desc `svp`.`user`;
select `account`, hex(`pass_digest`), `utime`, `ctime` from user;
-- select account, hex(pass_digest) as digest from user;
-- select account, hex(pass_digest) as digest from user where account="WangKaixuan1";
-- INSERT INTO `svp`.`user` (`account`, `pass_digest`, `utime`, `ctime`) VALUES ('WangKaixuan', 0x7846b0b3f881fa270a5a8cdd4691bcda09c1477b4393ef086ab3f0c7f7beb72c, now(), now());