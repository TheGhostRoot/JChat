### JCoreChat

Open-source social media application with customazation and everything you need to host it without knowing how to code. My first atempt was the YT-Downloader, but I want to go bigger and better so Welcome to **JCoreChat**.

**J** stands for Java, because Java will be the most used language for the project

**Core** means that the whole social media application with all the customazation is open-source and everyone from anywhere can make it batter

**Chat** because it is a social media platfort after all

### Plans

* It can use **SQL** and **NoSQL** for database. Tested databases: PostgresSQL, MongoDB, MySQL  | I wanted to add`ScyllaDB but I am too confused.
* Dockerfiles for easier and faster deployment
* .drawio files to explain how everything was planned with drawings ONLY for the network part
* Make sure every single feature can be custom in the config
* Make it easy to setup if any company wants to host it
* Make sure that everything is safe, secure and protected for both client and backed servers
* Make sure to make good and readable built-in docs for it so devs won't be confused
* The project will be free and open-source until I get paid to make it private

### Contribution

If you have kind heart and want to help me with this masive project. Please contact me in discord. Everyone is wellcome. I am solo developer for now and I am looking forward for teamwork. Don't worry if your work won't be notice. I will personaly ensure that you will be noted and notice.


### API Planning
2 APIs

* Application API | By dafault
- It is ment to be only used in the application.
- It is used only for login in the user and getting user's stats so the application can show it.
- Only users can use it. This mean that bots can't use users to get personal information or to login to their accounts.
- Has protection against spamming and abusing.

* Developer API
- It is ment to be only use by bots that are registered on the server as legit bots by developers. Users can't use this API.
- It is used only for **GROUPS** to customize user experience by using the API's doc. The API can be used in **Python**, **Java**, **C++**, **JavaScript** and **C#**
- Has protection against spamming and abusing.

Endpoints and Security information is in the docs: LINK IN THE FUTURE
