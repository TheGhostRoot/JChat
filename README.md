### JCoreChat

Template for chat application REST API with database.

**J** stands for Java, because Java will be the most used language for the project

**Core** means that it is open-source

**Chat** because it is made for social media platform after all

### Features

* It can use **SQL** and **NoSQL** for database. Tested databases: PostgresSQL, MongoDB, MySQL  | I wanted to add ScyllaDB, but it is too confusing.
* Roles with custom permissions. I haven't added checks for permissions based on role, because they are custom, so you have to implement them.
* I use custom base64 encoding and 2 types of encryption. Global and Per-User. Here is how the data is sent:
* Raw data -> encrypted (per-user or globally based on the use case) -> encoded with custom base64 letters. Then it is sent with headers.
* Make sure to make good and readable built-in docs for it so devs won't be confused.
* The API is made with in mind that only the original application will use it.
* 
### Contribution

Everyone is welcome!
