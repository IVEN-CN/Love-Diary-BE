create table tb_lover_group
(
    id          bigint auto_increment,
    user1_id    bigint
        constraint tb_lover_group_tb_user_id_fk
            references tb_user
            on update cascade on delete cascade,
    user2_id    bigint
        constraint tb_lover_group_tb_user_id_fk_2
            references tb_user
            on update cascade on delete cascade,
    create_time date default current_date not null
);

comment on table tb_lover_group is '情侣组表';

create unique index tb_lover_group_id_uindex
    on tb_lover_group (id);

create unique index tb_lover_group_user1_id_uindex
    on tb_lover_group (user1_id);

create unique index tb_lover_group_user2_id_uindex
    on tb_lover_group (user2_id);

alter table tb_lover_group
    add constraint tb_lover_group_pk
        primary key (id);

