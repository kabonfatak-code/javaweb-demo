use bbs;

alter table users add column province varchar(20) null after phone;
alter table users add column banned_until timestamp null after banned;
alter table users add column register_time timestamp null after created_at;

update users set register_time = created_at where register_time is null;

alter table users modify province varchar(20) null;
alter table users modify register_time timestamp not null;
