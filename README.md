## SecureDoc Full Stack Application
> Note: THIS README is still under going documentation and is not ready yet but will be updated as and when it is ready.
> However, it is still useful but you can always use it as a guild to understand the application adnwhat was done. 

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
This are the fields needed for the super class so that all the subclasses can inherit from this 
is also going to be an entity that jpa will manage for us, what we have to do now is add the annotation for the required fields.

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

### Presist and PreUpdate [Refactoring Stopped here]
- What we need to do now is implement the logic for prepersit or preupdate so that before we presit 
for any entity we check for all of the field we are setting because we cant save anything in the database 
if we dont have who created it or who updated it or the date they were created or updated. 

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

Today I have been able to make both the docker container and the pgadmin running. I am happy i was able to do it.
Now we will be able able be use the credentials to login without having to install them on our local machine. 
After we run the application with thses credentials we will go back to the pgadmin and our tables should show up 
on our database. 
So what we are going to be working on next is putting the config file into our appllication. 

## Application Properties.
Now we will be working on the database configuration to this application so that when we run it JPA will 
pick up all of our entities and find the config file and use it to create the tables in our database.
So we created a new Directory called `docker` and then we move both our `compose.yml` and our `.env` file into the
`docker` directory.
If we dont want to use the enviroment variables we can add the hard coded values instead. 
```yml
services:
  postgresdb:
    container_name: postgrescontainer
    image: postgres:16.3
    restart: always
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
    expose:
      - 5432
    ports:
      - 5432:5432
    volumes:
      - postgresvolume:/var/lib/postgresql/data
      - ./schema.sql:/docker-entrypoint-initdb.dl/schema.sql

  pgadmin:
    container_name: pgadmincontainer
    image: dpage/pgadmin4:latest
    restart: always
    environment:
      PGADMIN_DEFAULT_EMAIL: ${PGADMIN_EMAIL}
      PGADMIN_DEFAULT_PASSWORD: ${PGADMIN_PASSWORD}
      PGADMIN_DEFAULT_ADDRESS: 6000
      PGADMIN_LISTEN_PORT: 6000
    expose:
      - 6000
    ports:
      - 8000:6000
    volumes:
      - pgadminvolume:/var/lib/pgadmin

volumes:
  postgresvolume:
  pgadminvolume:
```
- Now the first thing we are going to do it remain the proeprties file to yml file because i like working 
with yml alot. i like the syntax of a yml file. 

Its important to define which profile is actieve when ever you have a springboot application becuase you can have dif 
config. 

- The we define some serilaization with Jaskson, jackson is mostly use for serialization.

- After we are going to define some data source properties. 
```YML
    datasource:
      url: jdbc:postgresql://${POSTGRESQL_HOST}:${POSTGRESQL_PORT}/${POSTGRESQL_DATABASE}
      username: ${POSTGRESQL_USERNAME}
      password: ${POSTGRESQL_PASSWORD}
```
- After we are going to pass in some JPA data. 
```yml
    jpa:
      open-in-view: false
      database-platform: org.hibernate.dialect.PostgreSQLInnoDBDialect
      generate-ddl: true
      show-sql: true
      hibernate:
        ddl-auto: update
      properties:
        hibernate:
          globally_quoted_identifiers: true
          dialect: org.hibernate.dialect.PostgreSQLDialect
          format_sql: true
```
- jpa:open-in-view: false: This setting disables the "Open Session in View" pattern, which can be a performance issue in certain scenarios.
- jpa:database-platform: org.hibernate.dialect.PostgreSQLInnoDBDialect: This sets the database dialect to PostgreSQL with InnoDB storage engine.
- jpa:generate-ddl: true: This setting enables automatic generation of database schema based on the JPA entities.
- jpa:show-sql: true: This setting enables logging of SQL statements to the console.
- jpa:hibernate:ddl-auto: update: This setting tells Hibernate to update the schema automatically when the application starts.
Thats all for the configuration file, what we are going to now is create a new directory named `data`
inside this directory we will define the data(data.sql and schema.sql) spring is going look for a data sql 
and a schema sql it will run the schema first then run the data. 

Inside these schema is where we create and define data such as tables and stuff and then in the data.sql 
thats where we do the data insertion like inserting into a table and all. By default spring is going yo use 
these two file and to be more organise we put them under the data directory., However spring will not be able to find 
them if we do not specify the where to find them. 
SO we are going to add another configuaration to the properties file in other to tell spring where to find 
them. 
```yaml
    sql:
      init:
        mode: never
        continue-on-error: false
        schema-locations: classpath:/data/schema.sql
        data-locations: classpath:/data/data.sql
```
The mode is set to never because we dont want to runt he application yet we have define the schema yet. 
We also specify the classpath because we are going to use the data and schema files in our application.

Also because we are going to be uploading file we need to define some servlet and tell the servelt we are going 
to be uploading a certain file size.
```yaml
    servlet:
      multipart:
        enabled: true
        max-file-size: 1000MB
        max-request-size: 1000MB
```
By default the servlet max file size is 1mb, however  we are going to be uploading more then 1mb file
size. 

Also Under Spring again we are goig to pass in some `mail` properties because we are going to be using
to verfiy our users.
```yaml
    mail:
      host: ${EMAIL_HOST}
      port: ${EMAIL_PORT}
      username: ${EMAIL_ID}
      password: ${EMAIL_PASSWORD}
      default-encoding: UTF-8
      properties:
        mail:
          mime:
            charset: UTF
          smtp:
            writetimeout: 10000
            connectiontimeout: 10000
            timeout: 10000
            auth: true
            starttls:
              enable: true
              required: true
      verify:
        host : ${VERIFY_EMAIL_HOST}
```
Because we are going to be using docker when we ever we are doing deployment so we have to define the port, so we are 
going to add a server properties. 

1. spring.profiles.active: This property specifies the active profile for your application. The default profile is set to dev, but it can be overridden by setting the ACTIVE_PROFILE environment variable.
2. spring.jackson: This section configures the Jackson JSON library. It sets the default property inclusion to non_null, and it configures various serialization and deserialization options.
3. spring.datasource: This section configures the data source for your application. It specifies the JDBC URL, username, and password for connecting to the PostgreSQL database. The values for these properties are set using environment variables.
4. spring.jpa: This section configures the JPA (Java Persistence API) properties for your application. It sets various options such as open-in-view, database-platform, generate-ddl, show-sql, and hibernate.ddl-auto.
5. spring.sql: This section configures the SQL initialization for your application. It specifies the mode, continue-on-error flag, and the locations of the schema and data SQL files.
6. spring.servlet.multipart: This section configures the multipart support for file uploads in your application. It sets the maximum file and request sizes.

So what we going to be working on next is definig the values for these properties.
```yaml
ACTIVE_PROFILE
POSTGRESQL_HOST
POSTGRESQL_PORT
POSTGRESQL_DATABASE
POSTGRESQL_USERNAME
POSTGRESQL_PASSWORD
EMAIL_HOST
EMAIL_PORT
EMAIL_ID
EMAIL_PASSWORD
VERIFY_EMAIL_HOST
```
## Creating the Tables with Spring JPA
After defining these values above the values was defined in a application-dev.yml file and also in 
application-prod.yml. This was done to make it easier for us to deploy our application to both dev and prod.
Having it on dev means we can run the application on dev by default and then run on prod when we deploy the application. 

After then i run the application and encounter some errors: 
1. Initially i on commented the postgres dependencies so the error was a datasource error, meaning it 
could not find the data source for the database.
2. This error is Caused by: org.postgresql.util.PSQLException: ERROR: relation "users" does not exist
Meaning the application was not able to find the user table in the database.
So what i am going to be working on now is basically finding the "Users does not exist error".

However the application did run after we uncommented the postgres dependencies and then the following tables 
were created in the database.
[User-roles. roles, credentials and confirmations]

## Fixing the "Users does not exist error"
So the application is unhappy with this: 
```java
    @Column(columnDefinition = "TEXT") //(userEntity)
```
It wanted us to put it to lowercase
```java
    @Column(columnDefinition = "text")
```
After that, the applicaition is now running and all the tables have been created also the 
primary-key-seq that we created also exist in the database.
![postgresDB tables.png](src%2Fmain%2Fresources%2Fassets%2FpostgresDBTables.png)

Tables ERD
![ERD.png](src%2Fmain%2Fresources%2Fassets%2FERD.png).

WHhat we will be working on next is creating a controller then accessing the services and sending the
information to save the users.

---

## Creating repositories
In other to create any user we need to intaract with the database and in other for us to do that we will create the `repo`, 
We will create this repo for all of our entities. 

We create the RoleRepository and it has an
```java
Optional<RoleEntity> findByName(String name);
```
For this repository, this is the only method we are going to have in here, we need to be able to featch a role 
entity by just passing in the method. 
Then we created tge UserRepository which also have a method to `getCredentialsEntitiesById` this helps us 
get the credentials entity by passing in the user id which belongs to the UserEntity.
```java
  Optional<CredentialEntity> getCredentialEntitiesById(Long userId);
```
Then we also created a `ConfirmationRepository` which has a method to `findByKey` this helps us to find 
a user by passing the Key and also we have another method `findByUserEntity` this helps us to find a user. 

```java
public interface ConfirmationRepository extends JpaRepository<ConfirmationEntity, Long> {
    Optional<ConfirmationEntity> findByKey(String key);

    Optional<ConfirmationEntity>findByUserEntity(UserEntity userEntity);
}
```
Then we also have the `UserRepository` which has a method to `findByEmail` this helps us to find a user by
and also another method `fingUserByUserId` this helps us to find a user by passing in the user id.
```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity>findByEmailIgnoreCase(String email);

    Optional<UserEntity>findUserByUserId(String userId);
}

```
Now that we have created ann 4 needed repo we are good to intaract with the database. What we will do next now 
is create the services and then we can call this repositories to get the information we need to create the 
functionalties that we want so that we can save the user. 

## Creating the UserServices
We created a UserServices interface class which has a method createUser that takes in a firstname, lastname, email 
adn password as a parameter. 
Because its an interface class, also created a UserServiceImpl class which serves as the implementation class for the 
UserService interface class. 
```java
public interface UserService {
    void createUser(String firstName, String lastName, String email, String password);
}

```

The `UserServiceImpl` implements the userservice and then has a method created user which is overriding the same method 
created in its interface class. 
```java
    @Override
    public void createUser(String firstName, String lastName, String email, String password) {

    }
```
We imported the neccssary repo as fields needed for this class
```java
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;
    private final BCryptPasswordEncoder encoder;
```
The private final `BCryptPasswordEncoder` fields is a from spring security, how ever initially i commented 
the dependency because we dont need it currently and i am just going to comment the field also because its not 
needed currently. 
We are just goigng to save the raw password for now. 
Lastly we inport the`ApplicationEventPublisher` because we need to publish an event when a user is created.

CreateUser
```java
    public void createUser(String firstName, String lastName, String email, String password) {
        userRepository.save(createNewUser(firstName, lastName, email));
    }
```
The method createUser takes in the parameter values, stores them in a userEntity then gets the password from the 
credentialEntity and saves it along side the password after that it verifies the userEntity through the ConfirmationEntity
and then created a publicEvent that helps to public an event to inform the other part of the system about the new user 
registration. 

```java
    @Override
    public void createUser(String firstName, String lastName, String email, String password) {
        var userEntity = userRepository.save(createNewUser(firstName, lastName, email));
        var credentialEntity = new CredentialEntity(userEntity, password);
        credentialRepository.save(credentialEntity);
        var confirmationEntity = new ConfirmationEntity(userEntity);
        confirmationRepository.save(confirmationEntity);
        publisher.publishEvent(new UserEvent(userEntity, EventType.REGISTRATION, Map.of("Key", confirmationEntity.getKey())));
    }
```
1. Creating a User Entity: Storing basic user information like name and email.
2. Storing Credentials Securely: Saving hashed passwords for secure authentication.
3. Account Verification (Optional): Implementing a system for verifying new user accounts (potentially using a confirmation entity).
4. Event Notification (Optional): Publishing an event to inform other parts of the system about the new user registration.

So the new thing i am going to be created is the `createnewUser` helper method, this method takes 
in the parameters firstname, lastname, email then gets the rolename from the Authority "User" after it then 
returns a createUserEntity method that takes in the firstname, lastname, email and role. 
```java
    private UserEntity createNewUser(String firstName, String lastName, String email) {
        var role = getRoleName(Authority.USER.name());
        return createUserEntity(firstName, lastName, email, role);
    }
```
The `getRoleName` method howeever, takes in the name as param then calls the roleRepository method 
`findByNameIgnoreCase` and pass in the name param, this helps to get the role from the database through 
the roleRepository.
```java
    @Override
    public RoleEntity getRoleName(String name) {
        var role = roleRepository.findByNameIgnoreCase(name);
        return role.orElseThrow(() -> new ApiException("Role is not found"));
    }
```

So when ever we wabt to create a user we are going to see if we can find the user role so by default we 
are going to give user the user role so that mean is that this role should already be in the database, so what we are going 
to do now is we need to something called "seed" "database seeding". Database seeding is the process of
adding data to the database for the first time before the application is running. so we will porpulate the 
database with some data

If we try to run the aplication now before it database seeding it will fail this is becasue 
the database seeding hasnt happend and it wont be able to find a role in the database to assign 
to the created User by Default. 

So now we nee to work on the `createUserEntity` method: 
```java
return createUserEntity(firstName, lastName, email, role); //Todo: Create the method
```
So in other to do this we head over to the utils package and then created a Userutils class in this class
we created a the default user that will will be saving in the database using the builder method and then 
import the method in the userServiceImpl.createNewUser method. 
So we can create a user now based on service perspective what we need to do now is create a controller to 
or a restcontroller thats going to allow us to expose these funtionalities over HTTP server. 

## Create a new Resource (Controller) for User

---
Created a new package called resource, this is the package for the controllers. 
## UserResources
This class is a controller class responsible for exposing the functionalities over HTTP server.
```java
  public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request) {

    }
```
The reason why we using the response is becuase we want to be able to always  return the same
response with the same format throughout the entire application. Whether its a success response or a 
failure response we what they to follow the same format. 
##### Response
After then we created the response class which is a `Java record class`
The objective of the Response class is to encapsulate the response data from an API endpoint
in a standardized and structured way. It provides a way to represent the response in a format
that is easy to understand and manipulate

```java
@JsonInclude(NON_DEFAULT)
public record Response(String time, int code , String path, HttpStatus status, String message, String exception, Map<?, ?> data) {

}
```

##### DTOrequest

The UserRequest class represents a data transfer object (DTO) that
is used to encapsulate the data sent in a user request. It contains several fields
representing the user's first name, last name, email, password, bio, and phone number.

The @NotEmpty annotations are used to validate that the fields are not empty or null 
when the UserRequest object is created. 
```java
 @NotEmpty(message = "First name cannot be empty or null")
    private String firstName;
```
The @Email annotation is used to validate that
the email field contains a valid email address.
```java
    @NotEmpty(message = "Email cannot be empty or null")
    @Email(message = "Invalid email address")
    private String email
```
After creating the two class `Response` and `UserRequest dto` we then head back back to the 
UserResource and import the class in the register endpoint. 
```java
    @PostMapping("/register")
public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request) {
        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());

        return ResponseEntity.created(getUri()).body(getResponse(request, emptyMap(), "Account created. Check your email to enable your account", CREATED));
        }
```
The @PostMapping("/register") annotation is used to map the HTTP POST method to the "/register" endpoint.

The saveUser method is the handler for this endpoint. It takes in a UserRequest object as a request body, which is validated using the @Valid annotation. The HttpServletRequest object is used to get information about the HTTP request.

Inside the method, the userService.createUser method is called to create a new user with the provided first name, last name, email, and password.

Finally, a ResponseEntity is returned with a Response object as the response body. The ResponseEntity.created method is used to set the status code to 201 (CREATED) and the location header to the URI returned by the getUri method. The getResponse method is used to create the Response object with the provided request, empty map, message, and status code

##### getURL 
The next thing we are going to be working on now is the `getUri` method and the will be in the 
utils package because its a helper method. 
This is one of the most important util in the application, this class is where we going to do everyrthing to make 
sure that we are always returning the same response all the time or everytime. 

The RequestUtils class contains a static method getResponse which is used to create a Response object. This object contains information about the request, such as the current date and time, the request URI, the HTTP status, a message, and the data.

The next time we wnat to do now is we need to be able to create some roles in the database so that when ever we create new user
the user will get a default role.

---
Date: 04/06/2024

So far we tested the application, we created a new user entity, however the user account is curretly locked
because when we created the user we sent a verification email to the user gmail and the user account gets inserted into the
database along side the user data but the user then need to verfify the emial before the account gets unlocked. 

So i had a few issue with the emial services where the service was unable to send the emial to the user, this took me a while to 
solve but this was due to the cinfiguration of the smtp email service and i was able to fix it.

After that the email service was able to send the emial to the user, what I am going to do now is to 
verify the user account. 

### User Account Verification
User need to be able to click on the link send to them and then comfirm the account, i am going to have 
to create a filter that is going to filter all the event in case the user is registering or resetting password, if the 
request is coming from a login user or if its a request that doesnt require a user then it will be ignored.. 

First we need to be able to activate our account so we are going to create another end points do that we can confirem the new
coount. After that I willl then work on  the filter so thbat i can set the userId automatically by using spring filter. And 
that is going to take me into spring security and other stuffs. 

So I create a new Api endpoint called `VerifyUser` handles a GET request to "/verify/account"
It expects a query parameter named "token" in the request. The method calls the verifyAccountKey method of the userService object,
passing in the value of the "token" query parameter. If the verification is successful, it returns a ResponseEntity object with a 200 O
K status and a response body containing a message indicating that the account has been verified.
```java
@GetMapping("/verify/account")
    public ResponseEntity<Response> verifyUser(@RequestParam("token") String key, HttpServletRequest request) {
        userService.verifyAccountKey(key);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account Verified. ", OK));
    }
```
The `verifyAccountKey` method overrides a method named verifyAccountKey in an interface or superclass.
It takes a String parameter named key. This method is likely used to verify a user's account by setting the enabled property of the 
corresponding userEntity object to true and deleting the corresponding confirmationEntity object.

It takes the key and then get the confirmationEntity for that key and then get the user from that 
confirmation and set the enabled property of the user to true, then the user repository save the user becuase we changed 
the enabled property of the user then  we delete the confirmationEntity.

So when we run the application and the perfrom a GET request to "/verify/account" with a query parameter named "key"
we got a response with a 200 status and a response body containing a message indicating that the account has been verified.
Then when we run it again to get the user, we got an error confimation key not found.
![img.png](src%2Fmain%2Fresources%2Fassets%2Fimg.png)
When we run again
![img_1.png](src%2Fmain%2Fresources%2Fassets%2Fimg_1.png)

Now we have a way to create a user and also confirm the new user account. Note that the user account 
are disables initially when they account is created. 

```java
  public static UserEntity createUserEntity(String firstName, String lastName, String email, RoleEntity role) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .mfa(false)
                .enabled(false)
            

    } 
```
### Creating the Security Filter Chain
First we created a security filter chain that helps to filter the request coming 
from the client and then send it to the next filter. However for testing purpose i want to 
able to permit some path to go through the filter so it doesnt get authenticated.
```java
  @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/user/login").permitAll() //For every http request that matches a specific pattern permit them.
                                .anyRequest().authenticated()) //Any other user that does match "Authenticate them"
                .build();
    }
```
### Creating the Authentication Manager
After creating the security filter chain we can now create the Authentiucation Manager, this will
help us to manager the authentication of the user, the authentication manager will recieve the
request for the filter chain and attemps to authentiate the user based on the provided credentials. 

```java
 public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(daoAuthenticationProvider);
    }
```
The Authentication manager use the authentication provider tohandle the authentication process thats 
why i am passing in the userdetailsservcies as a param. The authentication provider has a
`doauthenticationprovider` method that will attempt to authenticate the user so we have to pass in the 
userdetails to the dao authentication provider which will then attempt to `authenticate` the user.
The `DoaAuthenticationProvider` has an `authenticate` method, this method has `unauthenticated` and `authenticated`.

### PostMappinng method for login
```java
    @PostMapping("/login")
    public ResponseEntity<?>test(@RequestBody UserRequest user) {
        UsernamePasswordAuthenticationToken unauthenticated = UsernamePasswordAuthenticationToken.unauthenticated(user.getEmail(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(unauthenticated);
        return ResponseEntity.ok().body(Map.of("user", authenticate));
    }
```
This code snippet defines a POST endpoint for user login. It receives a JSON request body containing user credentials
(user.getEmail() and user.getPassword()). It creates an UsernamePasswordAuthenticationToken object with the provided 
email and password, and then authenticates the user using the authenticationManager. Finally, it returns a ResponseEntity with an OK status and a response body containing a map with the key "user" and the value of the authenticated user.
The `unauthenticated` method is called when the user does not exist in the database.
```java
    public static UsernamePasswordAuthenticationToken unauthenticated(Object principal, Object credentials) {
        return new UsernamePasswordAuthenticationToken(principal, credentials);
    }
```
The `authenticated` method is called when the user exists in the database.
```java
   public static UsernamePasswordAuthenticationToken authenticated(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        return new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
    }
```
### Breakdown
Now we have created our won Security Filter Chain, Authentication Manager, UserDetailsService and PostMapping for login, so we 
have taken the responsibility from spring security, now what i need to do next is create our own 
authentication provider so we can then pass it the userdatails comming from the database.


## Additional Notes 
### Testing Spring Security(Learning Before Impl)
So far we have been able to do this:
![img_2.png](src%2Fmain%2Fresources%2Fassets%2Fimg_2.png)
Though i havent done: User should be able to login into the application that because i havent created the UI
of the application but pretty much the backend for it has been created.

So the next thing we will be working on is the login functionalities.
![img_3.png](src%2Fmain%2Fresources%2Fassets%2Fimg_3.png)
In this login i am going to make use of spring security.

Note
For Securing our API, I will be using the Custom Token Implementation of Spring Security becuase spring
security is very powerful and its uses in alot on enterprise application.
### Custom Token Implememtation
So initially I had the dependency for spring security commented, so the first thing i will do is uncomment
the dependency then i will run the application.
After running the application a password was generated, and now i tried to get the the register user with the token
but it gave an `401 Unauthorized` error this is becuase we enables spring security and by default spring
gave us a username and a password which was generated when we run the application so now the best approach to getting the
user will be.
Choosing  the choosing auth and then using the basic auth option in postman, this will ask to enter the user name
and the password for authentication.
So the username will be `user` because it is by default and the password will be the password generated at runtime.
When we then send the request to get the user again we then, get the error user not found which I expected to get
because we already set the token to be deleted after verifying a user.
![img_4.png](src%2Fmain%2Fresources%2Fassets%2Fimg_4.png)
> Note
> By default spring security gives a form authentication with the basic auth so its basic and form authentication.  
> ![img_5.png](src%2Fmain%2Fresources%2Fassets%2Fimg_5.png)
>
> ![img_6.png](src%2Fmain%2Fresources%2Fassets%2Fimg_6.png)

#### Overriding the User Details Services
So what we going to do now is, instead of using spring default users and password, we are going to create our own
users like a custom user so we going to have to to override the default user manager system.
So what we will first did was create a new package called security then inside this secueity package,
everything related to the security is inside this package, so then we created an `FilterChainConfiguaration`
inside this class we created two custom users `daniel` and `james` they each have their username and password.
After we create these user we then return this user in other to override the default InMemoryUserDetailsManager
users.
Now we will run the aplication again and try to filln the form with these customer user credentials,
and now when we run it we got the same error without the unauthorized 401 error.
![img_7.png](src%2Fmain%2Fresources%2Fassets%2Fimg_7.png)
so now if i go to the browser and i then enter the credentials we will get something like this:
![img_8.png](src%2Fmain%2Fresources%2Fassets%2Fimg_8.png)
![img_6.png](src%2Fmain%2Fresources%2Fassets%2Fimg_6.png)
```java
 @Bean
    public UserDetailsService userDetailsService() {
        //First User
        var daniel = User.withDefaultPasswordEncoder()
                .username("daniel")
                .password("letdanin")
                .roles("USER")
                .build();
        //Second User
        var james = User.withDefaultPasswordEncoder()
                .username("james")
                .password("letjamesin")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(List.of(daniel, james)); //Override the user in memory user details with our custom user.
    }
```
So far we only did the User Details service part, the other three are still on the default (Filters, Authentication
Manager, Authentication Provider). The only thing weve override is the in memory user details.

#### Override the Authentication Provider and Authentication Manager
    In other fro use to override the `daoauthenticationProvider` we had to make use of the Authentication Manager
    because they work hand in hand with each other, so then the `daoauthentication` provider override the 
    `userDetailsProvider` values then return the new value. 
So when we run the application and pass in the same user details we got the same message as the last run, so we pass
the filter, so far we have overriden the `AuthenticationManager` and `AuthenticationProvider` with the same method
at the same time and then the `UserDetailServices`.
```java
  @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(daoAuthenticationProvider);
    }
```
By default all of the endpoint are locked by the springboot authentication, so what we hav to do now is
let springboot know we dont want all path (register, login, password) to go through the filter and only just
send it to the controller because we want the user to be authenticated in other to access the login or regsiter,
or reset password.
> All endpoints in the application are all secure by springboot, we can also tell spring security to not secure a
> specific endpoint and if a request comes in for a specify endpoint just let the request go through and then send
> the response back.

#### Add filter to open up some endpoints
So we created a `SecurityFilterChain` method that basically helps in filtering request so if a specify
request matches a specify pattern for examples url endpoint "user/test" it will permit the request however
other requst will be authorized.
```java
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/user/test").permitAll() //For every http request that matches a specific pattern permit them.
                                .anyRequest().authenticated()) //Any other user that does match "Authenticate them"
                .build();
    }
```
Run the application and send a GET request to:
```http request
http://localhost:8085/user/test
```
And you can see we got a 403 Forbidden error which mean we dont have permission this because we have no route defined in
the application, if we have a route define we will get a 500 server error but so far is it working.
![img_9.png](src%2Fmain%2Fresources%2Fassets%2Fimg_9.png)

So what we will do now is create a test endpoint  in the controller o(`userResource`), just to test the route and filter once
more. So now if we go back and re test the request we should get a 200 reponse back.
![img_10.png](src%2Fmain%2Fresources%2Fassets%2Fimg_10.png)
As you can see we got a 200 response back because we told spring to not secure the endpoint.

So what we want to do next is take control of the recieving the request to log a user inton the application,
we have some user define, we have some configuration going on but we are still not recieving any username
adn password, how can we get more control to do this, so we want to create a cmontroler son that we can send
our login user to this controller.

#### Recieving the request (User username and password)
We created a POSTMAPPING post request in other to login the user, so if we want to login we have to go
to `user/login`.
```java
    @PostMapping("/login")
    public String login(@RequestBody User user, HttpServletRequest request) {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(user.getEmail(), user.getPassword()));
        return ResponseEntity.ok().build();
```
This method acts as a login endpoint. It expects a POST request to "/login" with a request body containing user
credentials (likely in JSON format).
It attempts to authenticate the user using the provided username and password through Spring Security's AuthenticationManager.
If authentication is successful (specific logic might vary based on your implementation), it returns a successful
HTTP response (status code 200) without an additional response body.

So now we have taken control of the recieving the request to log a user into the application, we have our own
endpoint to login the user.
```java
  @PostMapping("/login")
    public ResponseEntity<Response>test(@RequestBody UserRequest user) {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(user.getEmail(), user.getPassword()));
        return ResponseEntity.ok().build();
    }
```

we have the AuthenticationManager to authenticate the user.
```java
public class UserResource {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
```
And we are calling the AuthenticationManager because we have define it in the `SecurityFilterChain` method.
```java
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(daoAuthenticationProvider);
    }
```
![img_11.png](src%2Fmain%2Fresources%2Fassets%2Fimg_11.png)
Now that we have taken control of the user login, we also have to take control of doing our own checks,
like checking if the password maches or not and if the password did not match we log a specific error, by defuly
spring has default password encoder that checks if the password matches and if not it returns a `bad credentials` message.
![img_12.png](src%2Fmain%2Fresources%2Fassets%2Fimg_12.png)
But how do we takes this functinalities from spring, how do we take control of it.

### Creating custm Authentication Provider
Whenever we are going to take control of the authentication provider we need to give spring security 
our own a userdetails and also an unauthenticated authentication which is going to be our `UsernamePasswordAuthenticationToken`

This authenticate method is responsible  for verifying the user's credentials and returning an authenticated Authentication object 
if the credentials are valid.

If the credentials do not match, the method throws a BadCredentialsException indicating that the user is unable to log in.
It compares the credentials the credentials of the user coming from the request with the credentials of the user retrieved from the database.
If the credentials match, the method returns a UsernamePasswordAuthenticationToken object with the authenticated user.

```java
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var user = (UsernamePasswordAuthenticationToken) authentication; //User coming in from the request.
        //Comparing both users from db and User from Request
        var userFromDb = userDetailsService.loadUserByUsername((String) user.getPrincipal()); //Load user
        //if the user credemtials is the same as user from Db credentials.
        if((user.getCredentials()).equals(userFromDb.getPassword())) {
            return UsernamePasswordAuthenticationToken.authenticated(userFromDb, "[PASSWORD PROTECTED]", null);
        }
        throw new BadCredentialsException("Unable to login");
    }
```
The next thing we have to work on now is the Authority, we need this because we are going to define
the role of the user in the database.

So now that we have created our own authentication provider we have to let spring knon we have our own 
authentication provider and since its a bean that makes it easier. 
In other to do that we need the tell the manager becuase its the one that is been used to do the authentication.
```java
 @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        MyOwnAuthenticationProvider myOwnAuthenticationProvider = new MyOwnAuthenticationProvider(userDetailsService);
        return new ProviderManager(myOwnAuthenticationProvider);
    }
```

So now we have our Security Filter chain, UserDetails, Authentication Manager, Authentication Provider. 
So now when we run the application we got a 403 error
> Tip 
> Keep in mind that Spring send 403 when ever there is any error in the application

Spring was failig because it wasnt able to do the comparison of the credentials, what i did to fix that is 
basically the password coming from the authority(users) to string. 
```java
var user = (UsernamePasswordAuthenticationToken) authentication; //User coming in from the request.
        //Comparing both users from db and User from Request
        var userFromDb = userDetailsService.loadUserByUsername((String) user.getPrincipal());
        //if the user credentials is the same as user from Db credentials.
        var password = (String)user.getCredentials(); 
        //if((user.getCredentials()).equals(userFromDb.getPassword())) {
        if(password.equals(userFromDb.getPassword())){
            return UsernamePasswordAuthenticationToken.authenticated(userFromDb, "[PASSWORD PROTECTED]", userFromDb.getAuthorities());
        }
```
And also because we have an encoder (defaultpasswordencoder), what i did to fix that is basically cancle our the 
password encoder and then pass in {noop} signifing that the password is not encrypted.
```java
    @Bean
    public UserDetailsService inMemoryUserDetailsManager() {
        return  new InMemoryUserDetailsManager(
                User.withUsername("Daniel")
                        .password("{noop}letjamesin")
                        .roles("USER")
                        .build(),
                User.withUsername("Daniel")
                        .password("{noop}letjamesin")
                        .roles("USER")
                        .build()
        );
    }
```
Then we rerun, because we dont have any password encoder at all we dont need the `{noop}`. 
We are able to login sucessfully: 
![img_15.png](src%2Fmain%2Fresources%2Fassets%2Fimg_15.png)
and if we provide a bad password, we get a 403 forbidden error
![img_16.png](src%2Fmain%2Fresources%2Fassets%2Fimg_16.png)

And that one approach to configure spring security for a more beoader understading I design a PKM explaining 
Spring Security. 
![img_17.png](src%2Fmain%2Fresources%2Fassets%2Fimg_17.png)



## Main Spring Security Implementation 
### Logic Features Intro
Todo: Load the Schema.sql and Data.sql in other to create the tables and add constraints.
### Authentication Part 1
Now we will implement the following functinalities using what i've learn 
in spring security. 
![img_13.png](src%2Fmain%2Fresources%2Fassets%2Fimg_13.png)

### Api Authentication
This is the equivalent of the usernamePasswordAuthenticationToken
It contains the user that we are going to be working with in the application when we are not dealing with the application or saving to the database.
We will have some kind of mapper that is going to take the user from the database and then maps
it to this class(User class) and vise versal.

So now that we have created the user class,we need to extend the authentitcation and make sure we can pput 
in the same pattern that we saw with `usernamepasswordAuthenticationToken`.
We extended the abstract authentication token because that class has some certain method that are just convinient working 
with. 

We created two constructor, the one we vare going to call when ever we are are going to start the authentication, 
```java
    public ApiAuthentication(String email, String password) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.password = email;
        this.email = password;
        this.authenticated = false;
    }
```
And the one we call when the user has been authenticated
```java
    public ApiAuthentication(User user, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = user;
        this.password = PASSWORD_PROTECTED;
        this.email = EMAIL_PROTECTED;
        this.authenticated = true;
    }
```
So we dont want to call the constructor diretly so we make them public and then create two public 
static `unauthenticated and authenticated method` 
The unauthenticated method is returning the email and password because that is what we passes in the request and when we staring the 
authentication.
```java
  public static ApiAuthentication unauthenticated (String email, String password) {
        return new ApiAuthentication(email, password);
    }
```
And the authenticated is returning the user and the authorities, we use this when the authentication is successful. 
We will call this so that we can create the actual fully authenticated user.
```java
    public static ApiAuthentication authenticated (User user, Collection<? extends GrantedAuthority> authorities) {
        return new ApiAuthentication(user, authorities);
    }
```
Also becuase we dont want the password to be seen we passed in the PASSWORD PROTECTED to the credentials.
We then created a `setAuthentication` method because we want to make sure that they call the authentication 
because we dont want anyone to be able to set the authentication with a setter so we have to make sure that.
```java
    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new ApiException("You cannot set authentication");
    }

```
Then we also create the `isAuthenticated` method and we retuen the autheticated boolean values for when ever we 
have a fully authenticated user. 
```java
    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }
```
So that is all for my authentication so i have my own class with my authentication and then when ever we 
want we can pass in email and the password whenm ever we have an unAuthenticated authentication or Api 
authentication or when ever the user is fully authentiacated we are going to call the constructor and pass in
the user and also authorities, spring by defauklt needs the authorities so that why we are passing in the `super`
```java
  private ApiAuthentication(String email, String password) {
        super(AuthorityUtils.NO_AUTHORITIES);
```
and also the contructors are private so that they can be use and we are forcing everyone to use the 
unauthenticated and then the authenticated helper method.Thats all we dont have to rely on the
`usernamepasswordauthenticationtoken` given by spring security. 

### Login Filter
What we want to do now is create a filter similar to the `UsernamePasswordAuthenticationFilter` this ist he filter 
class that intercept the request coming in and then it tries to do the authentication and spring is using a 
filter to do this so its only write that we also use a filter with the same pattern they had in mind. 

So now instead of having our endpoint in our resources we are nnot going to do it this way
```java
//Before
    @PostMapping("/login")
    public ResponseEntity<?>test(@RequestBody UserRequest user) {
```
We are going to have this endpoints in the filter and then we are going to authenticate all the request coming in using 
the filter so thats what I am going to be working on next. 

We created a new class `Authentication filter` extending the abstractAuthenticationProcessingFilter then reason why I am extending
this class is because it is a good class to use if you just want to do the authentication. 

After we implemented the `atemptAuthentication` this is the method that gets called when we want to initiates the authentication 
process. 

Then we created a constructor this helps whenever we want to pass something to the super class..
the authenticationFilter basically helps to tell the super class that for any Post Request that comes for the 
path(/user/login) then it's the ome it should listen-to to trigger the `attemptAuthentication` method.
```java
 public AuthenticationFilter(AuthenticationManager authenticationManager, UserService, JwtService jwtService) {
        super(new AntPathRequestMatcher("/user/login", POST.name()), authenticationManager);
    }
```
then we importted the userservice and the Jwtservice, the `userServices` for when we need to featch the user whenever
we get their username from the request and we need to update their login attemps and do some other things.

So now we need to define some method in the userservices so that we can update the login attempt and we are also going to 
need the JWT services so that we can pass in a token to the response. 
For the response we implement another method called the `successfulAuthentication` this gets called when ever the 
authenticamtion is successful.

So far the class is is listening on user login and then its going to call the `attemptAuthentication` and if the
authentication is successful, it going to call `successfulAuthentication`.

So now we are going to be working on the userServices so that we can update the login attempt of the user. 

### Filtering Login 2
The first thing we are going to do is define some ENUM, we created a class called `LoginType` this class
will give us the type of login that is happening.
```java
public enum LoginType {
    LOGIN_ATTEMPT, LOGIN_SUCCESS
}
```
Also I am not creating a converted for this enums because we are not saving these to the database so they dont 
need to be converted to columns and all. 

So in our userservices we created a new field called `updateLoginAttempt` we are creating this so we can basically update the 
login attempt so then we can update how many times a userlogin into the application to keep track so that we can lock 
their account. So it will update it in the database. 
```java
    void updateLoginAttempt(String email, LoginType loginType);
```
So now we will implement the method, in the method `updateLoginAttempt` we getting the user entity by email and also getting the
user id so in case something is saved in the database we know who did it and then we are also creating a sitch to switch 
between the loginTypes -> LOGIN ATTEMPT AND LOGIN_SUCCESS. 

In the LOGIN ATTEMPT -> if its a login attept we need to check to see if the user has already been in the **_cache_**.
The **_cache_** we dont have yet and still needs to create and then if the user is not the cache then we need to set 
their login attempt to ZERO(0) then give a message ('Your account not locked'). 

Otherwise we need to increase their login attempt and if their login attempt is bigger than 5, like 6 or greater then we 
are going to lock their account. 

In the LOGIN SUCCESS -> They are going to reset their account because we are going to set their login attempt to 
ZERO(0) and them remove them from the cache and set their last login to the current instance because that means they logged in
successfully. 

So what we need now is the cache because if we dont have the cache, their is not way we are going ton knw if the user
is in the **_cache_** of not and what login attempt that they have. 

### User Cache
So first go to your browser and type in `google guava maven`, click on the maven link showing first and 
click on the latest version, I would advise version 33.0.0jre because its still the most stable version. 
Then after the download head to your `pom` file and add the copied dependency.

Then after that we created a new package called cache, anything cache related is going to be inside of this cache 
package. Then we created a class called cacheStore that takes in a Key and a Value. 
A key and a value  is uaully what is needed when every we need to cache anything in an application.

Then we created a cnstructor called cacheStore, that takes in an expiry Duration and a TimeUnit, so 
when ever we create a construtor for the type we want to cache, it going to build the cache and then wait for 
a certain duration of time before it then expires. 

The cache store takes the key and the value for the type of cache that you need to creates, so if you need to pass
in a String, and then a User, the K(Key) will be the string and trhe V(Value) will be the User.
```java
public class CacheStore <K, V>{
```
so whatever value that we pass in the K and the V will be the value that is then passed into the catual cache. 
```java
 private final Cache<K, V> cache;
```
In other to create a cache with the use of tis constructor
```java
    public CacheStore(int expiryDuration, TimeUnit timeUnit) {
        //Example: Expiry duration = 5, TimeUnit = Min -> 5 min
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiryDuration,timeUnit)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }
```
- Getting from the cache
Then whenever we need to get a value from the cache, is going to be through(by) the type that was 
define as the value. Because we are getting the values and the key is going to be the key of the cache.
Example: Let say the values is a User and then the Key is a String so if you waant to get the `user` you
have to pass in the String which is the key. 
```java
   public V get(@NotNull K key) {
        log.info("Retrieving from Cache with Key {} ", key.toString());
        return cache.getIfPresent(key);
    }
```
- Putting into the Cache
```java
    public void put(@NotNull K key, @NotNull V value) {
        log.info("Putting into Cache with Key {} and Value {}", key.toString(), value.toString());
        cache.put(key, value);
    }
```
This method takes in the key and the value which are both needed in other to put something into the cache. 
- Removing from the Cache
```java
    public void remove(@NotNull K key) {
        log.info("Removing from Cache with Key {} ", key.toString());
        cache.invalidate(key);
    }
```
In we want to remove from the cache this method takes in a key and then helps to invalidate the 
cache based on the key.
So that is all about the cache store, how ever this is just a cache store we have to do an implementation 
of the cache store. Thats what we going to be working on next. 

### Cache Configuration 
Now that we have the cache store we can define bean for specific implementations, we can pretty much use this 
cache store for any key value pair that we want to cache. 
 We are going to create an other class in the same cache pakage, in the class we created a cache `userCache`
where we can pass in a key for the String and an Integer for the Value and its going to 
expire after 900 sec which is a min. All the values and entries of the cache are going to 
expire after 900 sec of the time that we put them in the cache. 

So now we have the cache and we can also create another cache if we want we can create as many
cache as we want.
```js
public class CacheConfig {
    @Bean(name = "userLoginCache")
    public CacheStore<String, Integer> userCache() {
        return new CacheStore<>(900, TimeUnit.SECONDS);
    }
```
But we will stick with this for now what we are going to do now that we have the cache is go back to
the login attempt method in the userserviceimpl class and imput our logic. 

So the logic updateLoginAttempt. we have a swtich statement that is going to check if the login type is
LOGIN ATTEMPT or LOGIN SUCCESS. 

In the case of -> LOGIN ATTEMPT:  it checks if the user not in the cache, and if the user is not in the cache, it then set the
login attemto 0 and then set the account non lock to true, meaning the user account is not locked. 
Otherwise, if the user is already in the cache meaning the user already tries to login then it increase their loginattempt 
by 1, and then put thier email and their loginattemps numbers in the cache. 
It also check if the user login attempt is greater than 5, if it is then it locks their account.
 Then we save the user
In the case of -> LOGIN SUCCESS: We set the account non lock to true, meaning the user account is not locked, 
set their login attempts to 0, and remove them from the cache. 
Then we save the user.

Now that we have done the updateLoginAttempt method we need to find a way to get the logic credentials from the HttpServletRequest
and pass that into the authentication manage so that we can trigger the authentication process.
```java
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        userService.updateLoginAttempt("danielolaibi@gmail.com", LOGIN_ATTEMPT);
        return null;
    }
```
- Update login credential from httpserveletrequest 
First we are going to define a new Request -> Login Request in the dto package, 
How that we have the request we will try to map the value from the request to the
email and the password in the Login Request. 

So after we created update login attempt, we then try to get the login credentials from the http request, in other for us 
to do that we have to create a new login request class that has the emial and password fields then map the 
email and password coming from the httpservlet request to the `loginRequest` email and password. 
```java
public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        try {
            //Grab the user information to create the authentication after getting the login types
            var user = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, true).readValue(request.getInputStream(), LoginRequest.class);
```
After maping the crediential we the call the userservices to update the login attempt by passing the email
gotten fromn the mapped credentials.
```java
  var user = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, true).readValue(request.getInputStream(), LoginRequest.class);
            userService.updateLoginAttempt(user.getEmail(), LOGIN_ATTEMPT);
```
Then we try to create the authentication object which takes an unauthenticated authentication, pass in the credentials and then 
get the authenticationManager and then authenticate the user.
```java
  var authentication = ApiAuthentication.unauthenticated(user.getEmail(), user.getPassword());
            //Pass the credentials to the authentication manager
            return getAuthenticationManager().authenticate(authentication);
```

### Error Response
Class(RequestUtils.java)
In the attempt authentication method we used a try catch block to handle the exception in case there is an error while trying to
authenticate the user.
So now we will work on the handleErrorResponse method, this method is to handle an error response by
writing the error response inside the response body of the http request,response.

- handleErrorResponse method
If the exception is an instance of AccessDeniedException, it calls the getErrorResponse method to get an error response object 
with the appropriate status code and error message. Then, it calls the writeResponse method to write the error response inside 
the response body of the response object.
This method provides a way to handle and respond to errors during the authentication process.

- The getErrorResponse method gets an error response based on the given parameter, it takes in  the 
request and reponse http servlet and an exception and a status code, it then checks if the reponse contentype 
to a application_json_values and also set the status code of the response, it then returns an error response object.
```java
   private static Response getErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception, HttpStatus status) {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        return new Response(now().toString(), status.value(), request.getRequestURI(), HttpStatus.valueOf(status.value()), errorResponse.apply(exception, status), getRootCauseMessage(exception), emptyMap());
    }
}
```

Then we created an helper method that helps to writeResponse the given handleErrorResponse method object to the 
output stream of the HttpServletResponse using an ObjectMapper.
This method is designed to handle the serialization of the response object and send it back as a response in an 
HTTP servlet context. The method ensures that the output stream is flushed after writing the response and catches any exceptions that occur during the process, rethrowing them as an ApiException.

Overall, all this was done so if we have an exception in the attenpt authentication method, we will get a well formatted error response.

So the next thing that we need to work on is the successful authentication process and for this we are going to need the JWT service so thats 
what we are going to be working in next. 

### JWT Services part 1
Todo: In these approah we are going to make sure we add the token in a secure only cookie. 
First we created an interface for JwtServices, then in side this interface we define the following methods, 
- CreateToken - This method helps to create a Jwt token for the USER. It takes in the user object and a 
funtion token object as a parameter. The aim of the funtion is to convert a jwt token to a string representation 
of the token, the method return a string representing the created token.
- extractToken - The method takes an  an HttpServletRequest object and a tokenType string as parameters.
The aim of the method is to extract the token from the request and return it as a string.
- addCookie - The method add a cookie to an HttpServletResponse object. The method add a cookie to the response with the 
user's token and token type. 
- getTokenData - This method extract token data from a JWT token. The method takes in a token as a string.
It takes a String token and a Function<TokenData, T> object as parameters. 
The Function<TokenData, T> object is used to convert a TokenData object to a generic type T.
The method returns the extracted token data.

So before the proceed with the method we have to define the token first, so in the domain package we created a token class, 
which has two fields one is the access and one is the refresh. 
The access token fields allow us to access the token and the refresh token allows us to refresh the token..

The next thing we want to also do is define the token data, so we created a new class tokenData inside this class,w e defined 
the fields, user, claims, valid and authorities.
The use is going to be the user associated with the token and the claims is going to be the claims associated with the token
and the valid is going to be a boolean that indicates if the token is valid or not and the authorities is going to be the 
authorities associated with the token.

Now that we have the token data we need to define the token type, which is going to be an enum so we 
will create an enum class inside this class we define the token type enum.
ACCESS, REFRESH, the access enum has a valu of "access-token" and the refresh enum has a value of "refresh-token"..
Then we define a field value which will allow us to later get the value of the token type .

JWT PART 2
So now we need to give an implementation for all these methods which are in the JWTService 
business layer package, so what we did fi rs₫t was head to the `jjwt gitgub lirary` and then copy the maven dependency
and added it to the POM file so now we have it in our class path as our dependencies.

Then we import the claims from the libabry in the `TokenData` class, then next thing we will do is give an 
implementation for the methods in the `JwtService` class and also a JwtConfig class. 
The Jwt config is going to fetch some configuration for us. 

In the Jwtconfig file class, we defien two values which are expirating and secret, the expiration is 
the expiration time of the token and the secret is the secret key for the token. However we did not directly 
pass in the values, what we did was pass in the values from the environment variables, then define the values fro the 
environment variables the application.yml file. We set the expiration to 5 days so the token will expire after 5 days. 
```java
public class JwtConfiguration {
    @Value("${jwt.expiration}")
    private Long expiration;
    @Value("${jwt.secret}")
    private String secret;
}
```
When we going to run the application then we will pass in the secret key.. 

After we head back to the JwtServicesImpl and extend the JwtConfig also implement the JwtService. 
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl extends JwtConfiguration implements JwtService { }
```
Then we implement the methods for this class, so that we can create a token and extract a token., add a cookie and get token data.

