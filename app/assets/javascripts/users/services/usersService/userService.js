/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Communicates with the server to get user related information and perform user related commands.
 *
 * @memberOf users.services
 */
class userService {

  constructor($q,
              $cookies,
              $log,
              biobankApi,
              User) {
    'ngInject'
    Object.assign(this, {
      $q,
      $cookies,
      $log,
      biobankApi,
      User
    })
    this.currentUser = undefined;
    this.init();
  }

  /**
   * If the token is assigned, check that the token is still valid on the server
   *
   * @private
   */
  init() {
    const token = this.$cookies.get('XSRF-TOKEN');

    if (!token) { return; }

    this.biobankApi.get(this.biobankApi.url('users/authenticate'))
      .then((user) => {
        this.currentUser = this.User.create(user);
        this.$log.info('Welcome back, ' + this.currentUser.name);
      })
      .catch(() => {
        /* the token is no longer valid */
        this.$log.info('Token no longer valid, please log in.');
        this.currentUser = undefined;
        this.$cookies.remove('XSRF-TOKEN');
      });
  }

  /**
   * Retrieves the currently logged in user from the server.
   *
   * @return {Promise<domain.users.User>} The currently logged in user. If the user has not logged in then the
   * promise is rejected.
   */
  retrieveCurrentUser() {
    return this.biobankApi.get(this.biobankApi.url('users/authenticate'))
      .then((user) => {
        this.currentUser = this.User.create(user);
        return this.currentUser;
      })
      .catch(err => {
        if (err.status === 401) {
          this.currentUser = undefined;
        }
        return this.$q.reject(err);
      });
  }

  /**
   * Returns the currently logged in user.
   *
   * @return {domain.users.User} This service caches the user that is logged in. If the user is logged in,
   * then the cached user is returned. If the user is not logged in, then the user is retrieved from the
   * server.
   */
  requestCurrentUser() {
    if (this.isAuthenticated()) {
      return this.$q.when(this.currentUser);
    }
    return this.retrieveCurrentUser();
  }

  /**
   * Returns the currently logged in user.
   *
   * Does not request the user from the server.
   *
   * @return {domain.users.User} This service caches the user that is logged in. If the user is logged in,
   * then the cached user is returned. If the user is not logged in, then `NULL` is returned.
   */
  getCurrentUser() {
    return this.currentUser;
  }

  /**
   * Returns the currently logged in user.
   *
   * Does not request the user from the server.
   *
   * @return {boolean} This service caches the user that is logged in. Returns `TRUE` if the user is logged
   * in.
   */
  isAuthenticated() {
    return !!this.currentUser;
  }

  /**
   * Attempts to login to the server with the credentials passed in.
   *
   * @param {string} email - the email address registered with the server for this user.
   *
   * @param {string} password - the password used by the user.
   */
  login(email, password) {
    const credentials = { email, password };
    return this.biobankApi.post(this.biobankApi.url('users/login'), credentials)
      .then(user => {
        this.currentUser = this.User.create(user);
        this.$log.info('Welcome ' + this.currentUser.name);
        return this.currentUser;
      });
  }

  /**
   * Sends a logout request to the Biobank server.
   *
   * @return {Promise<undefined>} If the promise resolves successfully, then the user has been logged out.
   */
  logout() {
    return this.biobankApi.post(this.biobankApi.url('users/logout')).then(() => {
      this.$log.info('Good bye');
      this.$cookies.remove('XSRF-TOKEN');
      this.currentUser = undefined;
    });
  }

  /**
   * Ends the user's session.
   *
   * This may be used for testing purposes.
   *
   * @return {undefined}
   */
  sessionTimeout() {
    this.$cookies.remove('XSRF-TOKEN');
    this.currentUser = undefined;
  }

  /**
   * Requests that a user's password be reset.
   *
   * @param {string} email - the email address registered with the server for this user.
   *
   * @return {Promise<undefined>} If the promise resolves successfully, then the user's password has been
   * reset.
   */
  passwordReset(email) {
    return this.biobankApi.post(this.biobankApi.url('users/passreset'), { email: email });
  }

}

export default ngModule => ngModule.service('userService', userService)
