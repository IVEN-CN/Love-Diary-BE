create table tb_bind_invite
(
    id           bigserial             not null
        constraint tb_bind_invite_pk
            primary key,
    from_user_id bigint                not null,
    to_user_id   bigint                not null,
    expire_time  TIMESTAMP             not null,
    used         boolean default false not null
);

comment on table tb_bind_invite is '情侣绑定邀请表';

comment on column tb_bind_invite.id is '主键id';

comment on column tb_bind_invite.from_user_id is '发起邀请的用户ID';

comment on column tb_bind_invite.to_user_id is '被邀请用户ID';

comment on column tb_bind_invite.expire_time is '过期时间';

comment on column tb_bind_invite.used is '邀请是否已被使用';

