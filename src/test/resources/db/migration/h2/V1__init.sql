-- H2-compatible definition
create table tb_memo
(
    id      bigint auto_increment
        primary key,
    user_id bigint default 1 not null,
    type    varchar(255)
        check (
            type in ('NICE_EVENT', 'BAD_EVENT', 'REMIND_EVENT')
            ),
    details text,
    date    date default CURRENT_DATE not null
);

COMMENT ON TABLE tb_memo IS '备忘条例';

COMMENT ON COLUMN tb_memo.id IS '主键id';
COMMENT ON COLUMN tb_memo.user_id IS '该条目对应user表中的用户id';
COMMENT ON COLUMN tb_memo.type IS '备忘类型';
COMMENT ON COLUMN tb_memo.details IS '具体记事内容';
COMMENT ON COLUMN tb_memo.date IS '日期';

-- H2 does not support "ALTER TABLE ... OWNER TO ..."

create unique index tb_memo_index_pk
    on tb_memo (id);

-- H2 does not support "COMMENT ON INDEX ..."
