/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * Communicates with the server to get user related information and perform user related commands.
 */
class userService {

  constructor($q,
              $cookies,
              $log,
              biobankApi,
              User,
              UrlService) {
    Object.assign(this, {
      $q,
      $cookies,
      $log,
      biobankApi,
      User,
      UrlService
    })
    this.currentUser = undefined;
    this.init();
  }

  /* If the token is assigned, check that the token is still valid on the server */
  init() {
    var token = this.$cookies.get('XSRF-TOKEN');

    if (!token) { return; }

    this.biobankApi.get(this.UrlService.url('users/authenticate'))
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

  retrieveCurrentUser() {
    return this.biobankApi.get(this.UrlService.url('users/authenticate'))
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

  requestCurrentUser() {
    if (this.isAuthenticated()) {
      return this.$q.when(this.currentUser);
    }
    return this.retrieveCurrentUser();
  }

  getCurrentUser() {
    return this.currentUser;
  }

  isAuthenticated() {
    return !!this.currentUser;
  }

  login(credentials) {
    return this.biobankApi.post(this.UrlService.url('users/login'), credentials)
      .then((user) => {
        this.currentUser = this.User.create(user);
        this.$log.info('Welcome ' + this.currentUser.name);
        return this.currentUser;
      });
  }

  logout() {
    return this.biobankApi.post(this.UrlService.url('users/logout')).then(() => {
      this.$log.info('Good bye');
      this.$cookies.remove('XSRF-TOKEN');
      this.currentUser = undefined;
    });
  }

  sessionTimeout() {
    this.$cookies.remove('XSRF-TOKEN');
    this.currentUser = undefined;
  }

  passwordReset(email) {
    return this.biobankApi.post(this.UrlService.url('users/passreset'), { email: email });
  }

}

export default ngModule => ngModule.service('userService', userService)
