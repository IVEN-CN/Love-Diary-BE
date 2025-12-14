alter table tb_user
    add lover_id bigint;

comment on column tb_user.lover_id is '伴侣id';
