-- auto-generated definition
create table tb_memo
(
    id      bigserial
        constraint tb_memo_pk
            primary key,
    user_id bigint default 1            not null,
    type    varchar
        constraint tb_memo_type_check
            check (
                type in ('NICE_EVENT', 'BAD_EVENT', 'REMIND_EVENT')
                ),
    details text,
    date    date   default CURRENT_DATE not null
);

comment on table tb_memo is '备忘条例';

comment on column tb_memo.id is '主键id';

comment on constraint tb_memo_pk on tb_memo is '主键';

comment on column tb_memo.user_id is '该条目对应user表中的用户id';

comment on column tb_memo.type is '备忘类型';

comment on constraint tb_memo_type_check on tb_memo is '备忘类型检查';

comment on column tb_memo.details is '具体记事内容';

alter table tb_memo
    owner to postgres;

create unique index tb_memo_index_pk
    on tb_memo (id);

comment on index tb_memo_index_pk is '主键唯一索引';