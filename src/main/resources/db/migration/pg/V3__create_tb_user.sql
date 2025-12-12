create table tb_user
(
    id        bigserial
        constraint tb_user_pk
            primary key,
    user_name varchar(255) not null,
    nick_name varchar(255),
    password  varchar(255) not null,
    birthday  date,
    avatar    varchar(255)
);

comment on table tb_user is '用户表';

comment on column tb_user.id is '主键id';

comment on constraint tb_user_pk on tb_user is '主键';

comment on column tb_user.user_name is '用户名';

comment on column tb_user.nick_name is '昵称';

comment on column tb_user.password is '密码';

comment on column tb_user.birthday is '生日';

comment on column tb_user.avatar is '头像';

create unique index tb_user__unique_index_user_name
    on tb_user (user_name);

comment on index tb_user__unique_index_user_name is '用户名唯一索引';

