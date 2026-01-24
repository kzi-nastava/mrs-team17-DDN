/* ============================================================
   1) Zameni SVE {noop} lozinke bcrypt hash-om za "test123"
   ============================================================ */

update users
set password_hash = '{bcrypt}$2a$10$KIXlE3pM9vDOMkMt2rtI8eYpZ0Z1K3j5kGZQ8oH1YvEJc8WQYt9iG'
where password_hash like '{noop}%';


/* ============================================================
   2) Zameni SVE placeholder bcrypt hash-eve istim hash-om
      (REPLACE_WITH_BCRYPT_HASH → test123)
   ============================================================ */

update users
set password_hash = '{bcrypt}$2a$10$KIXlE3pM9vDOMkMt2rtI8eYpZ0Z1K3j5kGZQ8oH1YvEJc8WQYt9iG'
where password_hash like '{bcrypt}$2a$10$REPLACE_WITH_BCRYPT_HASH%';


/* ============================================================
   3) Dodaj {bcrypt} prefix svima koji imaju čist $2a$ hash
      (za svaki slučaj, ako je nešto promaklo)
   ============================================================ */

update users
set password_hash = '{bcrypt}' || password_hash
where password_hash like '$2a$%'
  and password_hash not like '{bcrypt}%';


/* ============================================================
   4) Bezbednosna zabrana: {noop} više nikada ne sme u bazu
   ============================================================ */

alter table users
    add constraint users_password_no_noop
        check (password_hash not like '{noop}%');

/* 2) Zameni placeholder hash (robustno) */

update users
set password_hash = '{bcrypt}$2a$10$KIXlE3pM9vDOMkMt2rtI8eYpZ0Z1K3j5kGZQ8oH1YvEJc8WQYt9iG'
where password_hash = '{bcrypt}$2a$10$REPLACE_WITH_BCRYPT_HASH'
   or password_hash like '{bcrypt}$2a$10$REPLACE_WITH_BCRYPT_HASH%'
   or position('REPLACE_WITH_BCRYPT_HASH' in password_hash) > 0;
