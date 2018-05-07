-- user

-- :name create-user! :! :n
-- :doc creates a new user record
--  values :username, :password
INSERT INTO users
(username, password)
VALUES (:username, :password)

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id
AND is_active = true

-- :name delete-user! :! :1
-- :doc retrieve a user given the id.
DELETE FROM users
WHERE id = :id

-- :name update-user-password! :! :1
-- :doc retrieve a news given the id.
UPDATE users
SET
  password = :password
WHERE
id = :id

-- :name get-user-by-name :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE username = :username

-- :name get-all-users :? :*
-- :doc retrieve all users.
SELECT * FROM users
