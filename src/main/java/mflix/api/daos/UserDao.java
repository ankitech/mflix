package mflix.api.daos;

import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import mflix.api.models.Session;
import mflix.api.models.User;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class UserDao extends AbstractMFlixDao {

  private final MongoCollection<User> usersCollection;
  //DONE
  //TODO> Ticket: User Management - do the necessary changes so that the sessions collection
  // returns a Session object
  private final MongoCollection<Session> sessionsCollection;

  private final Logger log;

  @Autowired
  public UserDao(
      MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
    super(mongoClient, databaseName);
    CodecRegistry pojoCodecRegistry =
        fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    usersCollection = db.getCollection("users", User.class).withCodecRegistry(pojoCodecRegistry);
    log = LoggerFactory.getLogger(this.getClass());
    //DONE
    //TODO> Ticket: User Management - implement the necessary changes so that the sessions
    // collection returns a Session objects instead of Document objects.
    sessionsCollection = db.getCollection("sessions",Session.class).withCodecRegistry(pojoCodecRegistry);
  }

  /**
   * Inserts the `user` object in the `users` collection.
   *
   * @param user - User object to be added
   * @return True if successful, throw IncorrectDaoOperation otherwise
   */
  public boolean addUser(User user) {
    //DONE
    //TODO > Ticket: Durable Writes -  you might want to use a more durable write concern here!
    usersCollection.withWriteConcern(WriteConcern.MAJORITY).insertOne(user);

    return true;
    //TODO > Ticket: Handling Errors - make sure to only add new users
    // and not users that already exist.

  }

  /**
   * Creates session using userId and jwt token.
   *
   * @param userId - user string identifier
   * @param jwt - jwt string token
   * @return true if successful
   */
  public boolean createUserSession(String userId, String jwt) {
    //DONE
    //TODO> Ticket: User Management - implement the method that allows session information to be
    // stored in it's designated collection.
    Session session = new Session();
    session.setUserId(userId);
    session.setJwt(jwt);

    Bson query = new Document("user_id", userId);

    UpdateOptions options = new UpdateOptions();
    options.upsert(true);

    sessionsCollection.updateOne(query, new Document("$set", session), options);

    return true;
    //TODO > Ticket: Handling Errors - implement a safeguard against
    // creating a session with the same jwt token.
  }

  /**
   * Returns the User object matching the an email string value.
   *
   * @param email - email string to be matched.
   * @return User object or null.
   */
  public User getUser(String email) {
    //DONE
    //TODO> Ticket: User Management - implement the query that returns the first User object.
    User user = usersCollection.find(Filters.eq("email", email)).first();
    return user;
  }

  /**
   * Given the userId, returns a Session object.
   *
   * @param userId - user string identifier.
   * @return Session object or null.
   */
  public Session getUserSession(String userId) {
    //DONE
    //TODO> Ticket: User Management - implement the method that returns Sessions for a given userId
    Session session = sessionsCollection.find(Filters.eq("user_id", userId)).first();
    return session;
  }

  public boolean deleteUserSessions(String userId) {
    //DONE
    //TODO> Ticket: User Management - implement the delete user sessions method
    Bson query = new Document("user_id", userId);
    sessionsCollection.deleteOne(query);
    return true;
  }

  /**
   * Removes the user document that match the provided email.
   *
   * @param email - of the user to be deleted.
   * @return true if user successfully removed
   */
  public boolean deleteUser(String email) {
    // remove user sessions
    deleteUserSessions(email);
    //DONE
    //TODO> Ticket: User Management - implement the delete user method
    Bson query = new Document("email", email);
    usersCollection.deleteOne(query);

    //TODO > Ticket: Handling Errors - make this method more robust by
    // handling potential exceptions.
    return true;
  }

  /**
   * Updates the preferences of an user identified by `email` parameter.
   *
   * @param email - user to be updated email
   * @param userPreferences - set of preferences that should be stored and replace the existing
   *     ones. Cannot be set to null value
   * @return User object that just been updated.
   */
  public boolean updateUserPreferences(String email, Map<String, ?> userPreferences) {
    //DONE
    //TODO> Ticket: User Preferences - implement the method that allows for user preferences to
    // be updated.
    if(userPreferences == null) throw new IncorrectDaoOperation("Preferences cannot be null");
    Bson queryFilter = new Document("email", email);
    User user = usersCollection.find(queryFilter).iterator().tryNext();
    usersCollection.updateOne(queryFilter, set("preferences", userPreferences));


    //TODO > Ticket: Handling Errors - make this method more robust by
    // handling potential exceptions when updating an entry.
    return true;
  }
}
