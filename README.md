## Entity Listener for Spring Boot
[Entity: Auditable -> exception: ApiException -> domain: RequestContext]
## Todo for application
Need to audit everything we do for the application

Create an auditable (abstract) class this class can be extended to create an
entity listener for any entity. Auditable --> all entities will inherit from this class. so
when every we try to save an entity in the database all of the logic inside of the auditable
will be executed and if we dont have who is updating the data or creating the daa then the execution
will fail. (Ongoing). We can get information from this class but we cant set information from this class. 

```java
    private Long id; //This is going to serve all the subclasses so dont need to define an id from the subclasses.
    private String referenceId =  new AlternativeJdkIdGenerator().generateId().toString(); //Used to identity a specific resource in the database. Anytime we save an entity they get a refID
    private Long createdBy; // WHO CREATED IT.
    private Long updatedBy; //WHO UPDATED IT.
    private LocalDateTime createdAt; //WHO UPDATED IT at what time
    private LocalDateTime updatedAt; //WHO UPDATED IT at what time.
```
This is really all we need for the super class so that all the subclasses can inherit from this 
is also going to be an entity that jpa will manage for us. 

What we have to do now is add the annotation for the required fields.
-
- **Id**

```java
    @Id
    @SequenceGenerator(name = "primary_key_seq", sequenceName = "primary_key_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_key_seq")
    @Column(name = "id", updatable = false)
    private Long id; //This is going to serve all the subclasses so don't need to define an id from the subclasses.
    private String referenceId =  new AlternativeJdkIdGenerator().generateId().toString(); //Used to identity a specific resource in the database. Anytime we save an entity they get a refID

    @NotNull
    private Long createdBy; // WHO CREATED IT.
    
    @NotNull
    private Long updatedBy; //WHO UPDATED IT.

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt; //WHO UPDATED IT at what time

    @Column(name = "updated_at", nullable = false)
    @CreatedDate
    private LocalDateTime updatedAt; //WHO UPDATED IT at what time.
```
**Quotes** 
Explain each annotations and why you need them.

---

- What we need to do now is implement the logic for prepersit or preupdate do that before we presit 
for any entity we check for all of the field we are setting because we cant save anything in the database 
if we dont have who created it or who updated it or the date they were created or updated. 

- PrePersist and PreUpdate.
```java
 @PrePersist
    public void beforePersist() {
        
    }
```
So this method is going to be called before any entity is saved in the database.

The beforePersist() method is used to set the createdAt, createdBy, updatedBy, and updatedAt
fields of an entity before it is persisted. It also checks if a userId is provide
and throws an exception if it is not.

The beforeUpdate() method is used to set the updatedAt and updatedBy fields of an entity before
it is updated. It also checks if a userId is provided and throws an exception if it is not.

So before we save any entity both of the above code is going to run. 


---
The next time I want to work on is the exception handling

- ApiException
```java
public void beforePersist() {
        if (userId == null) { throw new ApiException("Cannot persist entity without user ID in Request  Context for this Thread ");}
```
So we will have to create a new class for exception handling.
```java
public class ApiException extends RuntimeException{
    public ApiException(String message) {super (message); }
    public ApiException() {super ("An error occurred"); }
}
```
The objective of this ApiException class is to provide a custom exception class that can be used to handle exceptions
that occur during the execution of API-related operations in a Java application.
By extending the RuntimeException class, instances of ApiException do not need to be caught or declared explicitly.
This makes it easier to handle API-related exceptions throughout the codebase.


---
"Implementing request context and userId handling in domain package"

The next thing we will be working on next is figuring out what 
```java
     var userId = 1L;
```
is going to look like, we need to have a way to always get, set and retreive the userId in the incoming
request context when the request comes in. We need to set it and then get it anytime adn anywhere 
in the application.
- Request context
The point is to access some information in every thread when the request comes in.
So we are going to creat a thread local and its going to give us a way to create 
variables in every thread so we can access those variables when and where ever we want adn they are private fields. 

Creat a domain class.
Domains are like classes we have in the application while enity is still classes but are things that 
we are persisting in the database, we calling them entity because they are being managed by
JPA and JPA calls them entities.
Domains are just regular classes that are not being persisted in the database by JPA. 

Stopped here today [thein3rovert @github](https://github.com/thein3rovert)
19/05/2024

Start the next day 21/05/2024
Working on the Domain Package and also the userId in the request context.
```java
 @PreUpdate
    public void beforeUpdate() {
        var userId = 1L;
        if (userId == null) { throw new ApiException("Cannot update entity without user ID in Request  Context for this Thread");}
    }
```
[Entity: Auditable -> exception: ApiException -> domain: RequestContext]
Created the request context class -> domain package. 
```java
private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>(); // Allows us to create userId variables in every thread
    // and also set and get the userid (its like a typeof the variables)
    private RequestContext() {
    }
    //Setting the variable to nul
     public static void start() {
        USER_ID.remove();
    } //Allows us to initialise everything.
    //SETTER AND GETTER FOR THE VARIABLES
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }
    public static Long getUserId() {
        return USER_ID.get();
    }
```
Since all the values in the request context are all static we can just call the 
request context then set and get the values from the request context.

The next thing we're going to do it make use of it inside the Auditable class, we are going to 
be getting the values and later we are going to be setting them whereever the request comes in 
as the values are going to be done in  a filter so where the request comes in we are going 
to have some filter to set the request, however we need to do some logic because depending
on the request we might not have a userId in every request for example when we are sending a 
request to login or register. 

```java
@PrePersist
    public void beforePersist() {
        var userId = RequestContext.getUserId();
```
```java
@PreUpdate
    public void beforeUpdate() {
        var userId =  RequestContext.getUserId();;
```
Now we have set the values of the userId which will be coming from the request context.
So before a user can persist an entity it has to have a userId and also 
before a user can update an entity it has to have a userId and also every other classes 
that we created are going tp inherit from the Auditable class that way all of the auditables
fields will also belong to that class and the presist and update will also be done in that class
before any other class is being saved or updated.

---
Next, Creating the user class which will inherit from the Auditable class.
[Entity: [Auditable, User] -> exception: ApiException -> domain: RequestContext]
This user class is going to represent the data(User) that will be stored(persisting) in the database.
- The first we going to be doing is extend auditable so once we do that the class inherit the 
fields from the auditable class that was previously created above. 
```java
package com.in3rovert_so.securedoc.entity;
public class User extends Auditable{
}
```
We're also going to keep doing this for all entity that we are going to be creating thats is going 
to be managed by JPA. 
- Defining the fields so far we have
```java
 private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private Integer loginAttempts; 
    private LocalDateTime lastLogin;
    private String phone;
    private String bio;
    private String ImageUrl;
```
The `fields loginAttempts and lastLogin` are going to be used to keep track of the user login attempts and 
the last time they logged in so it will be used to block them if they exceed a limit.

After that i added fields for Spring Security
```java
   private boolean accountNonExpired; 
    private boolean accountNonLocked;
    private  boolean enabled; 
    private boolean mfa; 
```
These fields helps to load the user from database and use some of the values to create
a user details that we can pass into spring security so that spring security can do auth for us.

We are also going to need to keep track of the secret QR code so we can determine if the code 
the user entered (verification code) is correct or not and we also need to save this value in a database 
and this value is going to be created every time set their multi factor authentication because thatas what 
we're going to use to determine if its a correct code or not.
```java
private String QrCodeSecret;
```
---
- User Entity Part 2 (Adding Annotations)
```java
@Getter
@Setter
@ToString
@Builder //Need to know what this is for
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") //Naming the table
@JsonInclude(NON_DEFAULT)
```
I also enforce some of the fields to be not null and also the id to be unique. 
```java
 @Column(updatable = false, unique = true, nullable = false) // We cannot have a user without an id
    private String userId;
```
```java
@Column(unique = true, nullable = false) //We cannot have a user without an email also
    private String email;
```
Everytime we deserialize an object from the database we want to get the id from the
database, and send it over as a JSON to the client(backend)
When ever we get a deta from a database its called deserialization. When we send the 
JSON to the client its called serialization.

> Serialization is converting an object to JSON and deserialization is converting JSON
to an object and this is what JPA is doing.

```java
   @JsonIgnore
    private String qrCodeSecret;
```
The reason why we are doing this is because when ever we want to send the Json to the client
we dont want to send the QR code with the data.
```java
 @Column(columnDefinition = "TEXT")  //Updating the column definition of the ImageURI because its a very long string.
    private String qrCodeImageUrl;
```
We are updating the column definition of the ImageURI because its a very long string and 
we want to save it in the database, having a very long string is not a good idea bacause this 
will take up a lot of space in the database and it will slow down the application.

- User Roles
The next things we need to do is adding roles to the user and also define a class that is going to be 
the class representing ther roles. Thats what we are going to be working onn next. 
 I rename the user clasds to UserEntity because that is what we are going to be using 
to have a simple user class that is going to map some of the object fields into 
an other object. The userEntity is going to be the entity it is never going to go to the 
frontend but the user class is what we are going to be sending over to the frontend.

- Now lets create the RolesEntity
  [Entity: [Auditable, UserEntity, Roles] -> exception: ApiException -> domain: RequestContext]
    ```java
    public class RoleEntity extends Auditable{
    private String name;
    private String authorities; 
}
    ```
Because we are goign to define authorities for each roles and they will be ENUM.
```java
private String authorities;
```
And then we import that RoleEntity into the UserEntity class so that we can use it
to create the roles and then I did some mapping to
```java
@ManyToMany
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();
```
Many to one means many users can have one role and one role can have many users because the 
roles they carry their permissions to do certain things. So depending your the roles that give
user a permission to do and not do certain things.
If i want a high level control i can just use the roles and if i want a refine grainnroles 
i can use the permissions and authorities associated with that roles. 

**_For examples:_** 
> ROLE(read, update, delete):
>   USER{read, update}
>   ADMIN{read, update, delete}

Much better way, meaning the user can read, update and reah documnets.

>   USER{user:read, user:update, document:read} 
>   ADMIN{user:read, user:update, user:delete}
>   MANAGER{document:read, document:update, document:delete, user:update}
> 
Back to the mapping
```java
  @ManyToOne(fetch = FetchType.EAGER) //Many user can only have one role,  the Eager means when ever we load a user, we want
    //load their roles.
    @JoinTable(
            name = "user_roles", 
            joinColumns = @JoinColumn (
                    name = "user_id", referencedColumnName = "id"
            )
    )
```
The @JoinTable will create another table in btw  to map the UserEntity with the RoleEntity in the database
so this table will have the name of user_roles so that we can clearly identity 
this table. Then we say "From this Jointable class, i want the name of the column 
to be user_id and the name of the referenced column to be id."
The "id" is going to be the id of the UserEntity which is going to be the foreign key. 
Which is this id: 
```java
 private Long id; 
```
Which is also the id of the RoleEntity.
```java
    private String userId;
```
And then the inverseJoinColumns is going to be the name of the column in the RoleEntity
```java
 @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn (
                    name = "user_id", referencedColumnName = "id"),
                    inverseJoinColumns = @JoinColumn(
                            name = "role_id", referencedColumnName = "id")
            )
    )
private RoleEntity role;
```
We are joing both the UserEntity and the RoleEntity in the database. The first joinColumns is going to be
is referencing the UserEntity and the second joinColumns is going to be referencing the RoleEntity fields. 
![joincolumn explain.png](src%2Fmain%2Fresources%2Fassets%2Fjoincolumn%20explain.png)
![joincolumn table.png](src%2Fmain%2Fresources%2Fassets%2Fjoincolumn%20table.png)

The next thing i want to do is define the enum to take care of the roles.
End 21/05/2024.

---
Formating CTRL + ALT + L
---
Date: 22/05/2024
TODO: Define the enum to take care of the roles and the table.
[entity: Auditable -> exception: ApiException -> domain: RequestContext -> enumeration: Authority]
So we created a new package called enumaration which has a class called Authority which is an "ENUM" class. 
```java
public enum Authority {
}
```
Then went into the RoleEntity and change the authorities field and change the `private String authorities;`
to `private Authority authorities;`
```java
    private Authority authorities; // Because we are going  to need to define authorires for each roles and they will be ENUM.
}
```
So thats going to be the authoority for the user roles.
Now we are just going to define some enum in the authoriets class.
```java
  USER(""),
    ADMIN(""),
    SUPER_ADMIN(""),
    MANAGER("");
```
These values are like string we need to have a way to get these strings, in other to o that we will 
define some fields and constructors also a getter in other the get the values.  
```java
   private final String value;
    Authority(String value) {
        this.value = value;
    }
    public String getValue() {
        return this.value;
    }
```
Now we will define the values what the values are going to be, in other to do that we created a constatnt 
class. Inside this constasnt class we are going to be defining the values of all the authorities. 
```java
public class Constants {
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORITY_DELIMITER = ",";
    public static final String USER_AUTHORITY = "document:create,document:read,document:update,document:delete";
    public static final String ADMIN_AUTHORITY = "user:create,user:read,user:update,document:create,document:read,document:update,document:delete";
    public static final String SUPER_ADMIN_AUTHORITY = "user:create,user:read,user:update,user:delete,document:create,document:read,document:update,document:delete";
    public static final String MANAGER_AUTHORITY = "document:create,document:read,document:update,document:delete";

}
```
First the ROLE_PREFIX is going to be the prefix of the role, More about it will be explained later.
- The AUTHORITY_DELIMITER is going to be the delimiter of the authorities, which is the litle comma "," dividing/separating the authorities.. 
- The USER_AUTHORITY is going to be the authority of the user.
- ADMIN_AUTHORITY is going to be the authority of the admin.
- SUPER_ADMIN_AUTHORITY is going to be the authority of the super admin.
- MANAGER_AUTHORITY is going to be the authority of the manager.
After that we copied the authorities of each user and pasted in the Authority class user field.
```java
public enum Authority {
    USER("USER_AUTHORITY"),
    ADMIN("ADMIN_AUTHORITY"),
    SUPER_ADMIN("SUPER_ADMIN_AUTHORITY"), //These values are like string we need to have a way to get these strings.
    MANAGER("MANAGER_AUTHORITY");
```
What we have mto figure out now is mapping this value from some data coming from my database
because we need to map these values if we going to be working with data in the database, as of now we dont 
have a way to remove these values from here and we are going to need to do that when ever we are mmapping things and 
sending reposnse back to the frontend or to any http client that calls this server. 

Next we going to see if we can define a converter for these values: 
```java
public enum Authority {
    USER("USER_AUTHORITY"),
    ADMIN("ADMIN_AUTHORITY"),
    SUPER_ADMIN("SUPER_ADMIN_AUTHORITY"), //These values are like string we need to have a way to get these strings.
    MANAGER("MANAGER_AUTHORITY");
```
Because we are going have a way to convert these values: 
```java
 public static final String USER_AUTHORITY = "document:create,document:read,document:update,document:delete";
    public static final String ADMIN_AUTHORITY = "user:create,user:read,user:update,document:create,document:read,document:update,document:delete";
    public static final String SUPER_ADMIN_AUTHORITY = "user:create,user:read,user:update,user:delete,document:create,document:read,document:update,document:delete";
    public static final String MANAGER_AUTHORITY = "document:create,document:read,document:update
```
from the database to the java class or the enum and visa versal.
> [AI GENERATED
> 
> Here, each authority (USER, ADMIN, SUPER_ADMIN, MANAGER) is assigned a corresponding value (USER_AUTHORITY, ADMIN_AUTHORITY, SUPER_ADMIN_AUTHORITY, MANAGER_AUTHORITY)
>The getValue() method allows you to retrieve the value associated with each authority.
>Additionally, the Constants class defines constants for the authority values. These constants can be used to map the values from the database to the Authority enum and
>vice versa.
>To map the authority values from the database to the Authority enum, you can create a converter. This converter will convert the database values to the corresponding Authority enum instances and vice versa.
---
- What we want to do now is to create a converter for these values.
create package inside enum package, called the `converter`, this package has a java class 
called `RoleConverter`. 
```java
public class RoleConverter implements AttributeConverter<Authority, String> {
    @Override
    public String convertToDatabaseColumn(Authority authority) {
        return null;
    }
    @Override
    public Authority convertToEntityAttribute(String s) {
        return null;
    }
}
```
Because we implemented the `AttributeConverter` interface, we need to implement the 
AttributeConverter interface we need to add the convertToDatabaseColumn method and 
the convertToEntityAttribute method this helps us to convert these values from the database to the java class or the enum and visa versal.

- convertToDatabaseColumn: convertToDatabaseColumn, is an implementation of the AttributeConverter interface's convertToDatabaseColumn method.
It's used in the context of JPA (Java Persistence API) to convert an Authority object into a string representation that can be stored in a database column

- convertToEntityAttribute:This will take whatever values we give it or get from the database and convert them to
the enum and convert ut to entityAttribute.
```java
  @Override
    public String convertToDatabaseColumn(Authority authority) {
        if (authority == null) {
            return null;
        }
        return authority.getValue();
    }
```
- convertToEntityAttribute, is an implementation of the AttributeConverter interface's convertToEntityAttribute method.
It takes a String parameter code and returns an Authority enum value.

```java
@Override
    public Authority convertToEntityAttribute(String code) {
        if(code == null) {
            return null;
        }
        return Stream.of(Authority.values())
                .filter(authority -> authority.getValue().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
```
The method checks if the code is null. If it is, it returns null. If not, it uses Java 8's Stream API to find the first
Authority enum value whose value field matches the code string. If no match is found, it throws an IllegalAccessError.
In essence, it's a utility method to convert a string value into the corresponding Authority enum value.

Now we need to define some annotations for the converter. 
```java
@Converter(autoApply = true)
```
This just means that the converter will automatically run the converter when ever we are loading the class from the 
database. 
So when ever we are going to convert the Authority to enum to the database, we are going to 
get the string value. 
```java
  @Override
    public String convertToDatabaseColumn(Authority authority) {
        if (authority == null) {
            return null;
        }
        return authority.getValue();
    }
```
Thats what we are going to save and then since we save string values in the database, we are just going to map
them back to the authority where the values match.

```java
public Authority convertToEntityAttribute(String code) {
if(code == null) {
return null;
}
return Stream.of(Authority.values())
.filter(authority -> authority.getValue().equals(code))
.findFirst()
.orElseThrow(IllegalArgumentException::new);
}
```

---
#### Password 
Now if we go back to the UserEntity fields we are missing a field for the `Password` adn if we dont have 
a password then we cant really save a user and if we cant save a user they wont be able to login. The reason for 
this is going to be that the password is going to be its own class. Its going to be a stand alone class and 
we are going to work out it's logic. 

First we created a credential class for handling the credentials of the user in the class we have these fields. 
```java
public class CredentialEntity extends Auditable {
    private String password;
    private UserEntity userEntity;
```
Then we define a construtor for the fields. 
```java
public CredentialEntity(String password, UserEntity userEntity) {
    this.password = password;
    this.userEntity = userEntity;
}
```
Annotations
```java
public class CredentialEntity extends Auditable {
    private String password;
    @OneToOne(targetEntity = UserEntity.class, fetch = EAGER) //When ever we load the userEntity it will load
    //all user associated with the credentials.
    @JoinColumn(name = "user_id", nullable = false) //We need the id specifically so we its just going tom
    //get the user_id from the userEntity.
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("user_id")
    private UserEntity userEntity;
```
@OneToOne: This annotation indicates that the relationship is a one-to-one relationship.
@JoinColumn: This annotation specifies the name of the column in the database that will be used to join this entity with the UserEntity entity.
@OnDelete: This annotation specifies the action to be taken when the referenced UserEntity is deleted. In this case, it is set to CASCADE, meaning that the referenced UserEntity will be deleted along with this entity.
@JsonIdentityInfo: This annotation is used for JSON serialization and deserialization. It specifies the generator and property for the identity of this entity.
@JsonIdentityReference: This annotation specifies that the user_id property should always be serialized as an ID reference.
Now these credeintials is associated with the userEntity so aytime we are going to save a new
credntial we need to give it a user.

Below is a better illustation of how this works.
![credential.png](src%2Fmain%2Fresources%2Fassets%2Fcredential.png)

So now we can represent this whole class as the credentials for the user. 
---
- The next thing we going to do is the confirmation, because whenever someone creates a new 
account we need to create some kindof a token or a key to confirm that the user is who they say they are.
and then we are going to save it in the database and send them an email with the key and password and we need to manage them 
in the database. Thats what we going to be working on next. 

#### Confirmation Entity
This is pretty similar to the credentials entity, in this class
```java
public class ConfirmationEntity extends Auditable {
    private String key; // ? This is going to be a like a UUID that we will sent to the user as a token
```
We have a field for the key, this key is going yto be a UUID that will be sent to the user, 
when they sign up
```java
 public ConfirmationEntity(UserEntity userEntity) { // We dont need to get the key because they are going to generate it for us.
        this.userEntity = userEntity;
        this.key = UUID.randomUUID().toString(); //When ever we create a new instance of this confirmation, it going to
        //automaticallt generate the key. 
    }
```
The UUID is what will be saved in the database as the id for the confirmation.

#### Email Services 
When ever someone creates a new user we will send them an email to confirm their account otherwise
they wont be able to login. That means we need to have an email service so we can send an email 
to the user when ever we create a new account for them with a link so when they click on it
they can activate their account.

We created a new emial services package. In this package we habe a `sendNewAccountEmail` method and 
`sendPasswordResetEmail` method.
```java
 void sendNewAccountEmail(String name, String to, String token);
    void sendPasswordResetEmail(String name, String to, String token);
```
And they have the same parameter, the name of the user, the email of the user, and the token. In other 
to create the implemetation for these methods we will need to create a package for the implemetation..
Then we created an `EmailServiceImpl` class which will have a Bean because we want to inject it 
to all the classes in the application.
```java
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    @Override
    public void sendPasswordResetEmail(String name, String to, String token) {
    }
    @Override
    public void sendNewAccountEmail(String name, String to, String token) {
    }
}
```
In other to make it a bean we added 
```java
@Service
@RequiredArgsConstructor
```
We dont have a JavaMailSender so we need to ad the `JavaMailSender` dependency to the Pom.xml and this dependency
is coming from the `spring-boot-starter-mail` library.
Inside the Pom.xml we need to add the following dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```
After i added two more fields 
```java
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.verify.host}")
    private String host;
    private String fromEmail;
```
The host is used do identify what my enviroment is when i am generating the link for the email because 
i dont want to use localhost or static ip-address, i need these to come from a properties file or something 
similar. Added the @value annotation to the `host` field because we are going to use it in the email service
to get the host from the properties file.
```java
  public void sendNewAccountEmail(String name, String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("NEW_USER_ACCOUNT_VERIFICATION"); 
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(getEmailMessage(name, host, token));
            sender.send(message);
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("Unable to send email"); 
        }
    }
```
The method takes three parameters: name (a string representing the name of the user), email (a string representing the email address of the user), and token (a string representing the password reset token).
Inside the method, a SimpleMailMessage object is created to represent an email message. The subject of the email is set to "NEW_USER_ACCOUNT_VERIFICATION". The sender's email address is set to the value of fromEmail, and the recipient's
email address is set to the value of email. The email message text is set by calling a method called getEmailMessage with the name, host, and token parameters.
Finally, the send method of the sender object is called to send the email message. If any exception occurs during the process, the exception message is logged, and an ApiException is thrown with the message "Unable to send email".

Similar thing is done for the sendResetPasswordEmail method. Inside the two method we have new method called the 
getEmailMessage and the getResetPasswordMessage, this method is going to be used in the two methods above
to create the email message text and the reset password message text.

SO the next thing we have to do is make sure that both these methods are asynchronous because 
we dont want to wait for the email to be sent to the user.
```java
@Async
    public void sendNewAccountEmail(String name, String email, String token) {
```
```java
  @Async
    public void sendPasswordResetEmail(String name, String email, String token) {
```
What these it going to do is run all these method in a separate thread when ever we call it so we dont 
have to wait for one to finsh the execution of the  thread continue. 

The next thing we have to do is make sure that we can define the `getEmailMessage and the getResetPasswordMessage`
method they are going to be a Util method. 

Stopped here today [thein3rovert @github](https://github.com/thein3rovert)
22/05/2024
---
Date: 23/05/2024
TODO: Working on Email services method (Email Utils)
First we create a new package for the util methods, then we create a new class called EmailUtils.
```java
   public static String getEmailMessage(String name, String host, String token) {
        return "Hello" + name + ",\n\nYour new account has been created. Please click on the link below to verify your account.\n\n" +
                getVerificationUrl(host, token) + "\n\nThe Support Team";
    }
```
Its taking the name the age and the token and its concatenating all so that it can give us a string.
The method takes three parameters:name (a string representing the name of the user), host (a string representing the
host of the user), and token (a string representing the password verification token).
We also have the getVerification method which is responsible for geting the verification from 
an endpoint. 
```java
    private static String getVerificationUrl(String host, String token) {
        return host + "/verify/account?token=" + token;
    }
```
We are going to also be creating the endpoint in our controller but later when we have the react application 
we are going to have to pass in the react application base as the host because they have to go to 
the frontend for some loading animations. 
We also do similar thing to the other method `getResetPasswordMessage` because they both have the same, parameters. 
so i only changed the method name.

- The next thing we wnat to do is create a way to call this method so we can send different email to the user, 
in other to do this we will need to use something called the eventlistener like an `event in spring` so that when someone create
a new account we will just fire the event and send the email to the user.
And thats what i am going to be working on for today.
So inside the Enumeration package, a new class was created called the EmailService`EventType`  this helps us 
to identify the different type of events and we are going to have just two events. 
```java
public enum EventType {
    REGISTRATION, RESETPASSWORD
}
```
for now these two event should be enabled.
```java
public class UserEvent {
private UserEntity user;
private EventType type;
private Map<?, ?> data;//
}
```
When ever we fire an event we can optionally map in some data. If we dont have  any data we don't have to pass in anything. But anytime we fire a new user event we have to give
the user the type and any data associated with that event. Now that we have the event we have to create 
the evenlistener. 
So we created a package called `event` inside this package we have the UserEvent class that we created earlier.
which is responsible for creating the event. Inside the same package we created a new package called the `listener`
which containers the `UserEventListener` class. 
The `UserEventListener` class is responsible for  handle UserEvent events and perform actions based on the type of
 the event. In this case, it sends emails to users based on the type of the event(Registation or ResetPassword).
```java
    public void onUserEvent(UserEvent event) {
        switch (event.getType()) {
            case REGISTRATION -> emailService.sendNewAccountEmail(event.getUser().getFirstName(), event.getUser().getEmail(), (String)event.getData().get("key"));
            case RESETPASSWORD -> emailService.sendPasswordResetEmail(event.getUser().getFirstName(), event.getUser().getEmail(), (String)event.getData().get("key"));
            default -> {}
        }
    }
```
The onUserEvent method is the main method of this class. It takes a UserEvent object as a parameter and performs different actions based on the type of the event.
It uses a switch statement to determine the appropriate method to call on the emailService object. For a REGISTRATION event, it calls sendNewAccountEmail with the 
user's first name, email, and a key from the event's data. For a RESETPASSWORD event, it calls sendPasswordResetEmail with the same information.

Thats all for the EventListners also the @EventListener annotation is used to listen for events which
helps us to fire an event.

#### Database Config
What we need to do now is pass in some configurations for the Database, because when ever we run this 
application we need to passin some information to the database such as the entity so we are going to need 
to have some JPA configuration so that spring knows what it will look for in our configuaration so that it will
create the data source for us otherwise all these classes are going to fail so we need to pass in the database configuration 
for that. 

So the next thing we need to do is work on the configuaration for our datasource. 

---
So I am going to be using postgresql as the database so all of our data are going to be saved using in this relational 
database system and i am going to be using pgaddmin to give me a UI interface so that i can work interact with the SQL 
server. 
Install Postgress, Pgadmin and PostMan for testing the API make sure to have these things on your computer, however in these
project i am ging to be using docker so that I dont have to download things on my computer and also get 
experience working with docker because i want to build my skills with docker and also use docker to get 
an instance of PGadmin. 

1. The first thing i want to do is create a compose file that is going to get me both postgresql and pgadmin running so that
i can run the docker container. 
```bash
code compose.yml
```
The First thing we have to do is define the services [List of services]
```yaml
services:
 services: 
  postgresdb: # Define the postgresdb service
    container_name: postgrescontainer  # Set the name of the container to postgrescontainer
    image: postgres:latest #Need to change the version
    restart: always # always what docker to run on machine start
    environment: # Passing in some env variable becuse we want to pass in some default username,pasword and DB
      POSTGRES_USER: thein3rovert ${POSTGRES_USER} # For security so that we dont pass in plain text
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    expose:
      - 5432 # Expose from inside of the container
    ports:
      - 5432:5432 # Map the port to the machine port
    volumes:
      - postgresvolume:/var/lib/postgresql/data # Mount a values for when container destroyed we run a new container witht the same value
      - ./schema.sql:/docker-entrypoint-initdb.dl/schema.sql
```
> Never use latest when you are building a project you are going to run on other computer unless 
> you are building your own project. Becuase latest is going to be out of date and this will cause 
> issues with project especially is your not maintaining the peoject it is a bad idea to use latest. 
> But if you are maintaining it, you can use latest.

2. The next is the Pgadmin service
```yml
gadmin:   
    container_name: pgadmincontainer   
    image: dpagel/pgadmin4:latest  
    restart: always   
    environment:  
      PGADMIN_DEFAULT_EMAIL: thein3rovert ${PGADMIN_EMAIL}  
      PGADMIN_DEFAULT_PASSWORD: ${PGADMIN_PASSWORD}
      PGADMIN_DEFAULT_ADDRESS: 6000
      PGADMIN_LISTEN_PORT: 6000
    expose:
      - 6000  
    ports:
      - 7000:6000 #Mapping the 6000 to my local host 7000
    volumes:
      - postgresvolume:/var/lib/pgadmin
```
So we have an instance of Postgres and Pgadmin with appropriate configurations.

Stopped: 23/05/2024 

---
Next thing we going to be working on is the .env file.

What we are going to be working on next is defining the enviroments variables for the our progres enviroments. 
So i basically created a .env file and then define the values to the environment variables so that docker will 
be able to use them, by be default docker is going to look for those file so we dont need to pass in any arguemene. 
We now have our dockerfile and `.env` file, the next thing we want to do now is run the docker container in terminal. 

## Running the docker compose file
To run the docker container
```bash
docker compose -f ./compose.yml up -d
```
Make sure to cross check the config file before running to mitigate any error, after wait for docker to pull the images. 
![Screenshot 2024-05-28 203031.png](src%2Fmain%2Fresources%2Fassets%2FScreenshot%202024-05-28%20203031.png)
To check the port docker is running on
```bash
docker ps | grep post 
```
on windows powershell which is what i am using
```bash 
docker ps | Out-String | findstr /i "post"
```
These shows us the port of both postgres amd pgadmin, so the next thing we want to do is: 
```bash 
hostname -I 
```
These shows us the internal ip-address so we can access it, how ever for window powershell, 
```bash
ipconfig
```
After look for the IPV4 address and enter it in your browser with the config open post 
for pgadmin. 
```http request
http://192.168.0.10:7000/login?next=/
```
Doing that will get an instance of pgadmin running in your browser. 

So what we need to donow is connect to the posgres database that we defined. 
So i try to connect to my postgres instance with pgadmin but it seem to be ignoring my passoword
and possible my username which i added already to my config file so what do i need to do. 
I think i will have to delete both images and pull them again with the config file. 
So that why i will be working on next. 

















