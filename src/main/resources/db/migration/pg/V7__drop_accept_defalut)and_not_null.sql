alter table tb_bind_invite
    alter column accept drop not null;

alter table tb_bind_invite
    alter column accept drop default;
