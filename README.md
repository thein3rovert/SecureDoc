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
