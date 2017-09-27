/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash', 'tv4'], function(_, tv4) {
  'use strict';

  UserFactory.$inject = [
    '$q',
    '$log',
    'biobankApi',
    'ConcurrencySafeEntity',
    'DomainError',
    'UserState',
    'UserMembership'
  ];

  /*
   * Angular factory for Users.
   */
  function UserFactory($q,
                       $log,
                       biobankApi,
                       ConcurrencySafeEntity,
                       DomainError,
                       UserState,
                       UserMembership) {

    var SCHEMA = {
      'id': 'User',
      'type': 'object',
      'properties': {
        'id':           { 'type': 'string' },
        'version':      { 'type': 'integer', 'minimum': 0 },
        'timeAdded':    { 'type': 'string' },
        'timeModified': { 'type': [ 'string', 'null' ] },
        'name':         { 'type': 'string' },
        'email':        { 'type': 'string' },
        'avatarUrl':    { 'type': [ 'string', 'null' ] },
        'state':        { 'type': 'string' },
        'roles':        { 'type': 'array', items: 'string' },
        'membership':   {
          'oneOf': [
            { 'type': 'null' },
            { '$ref': UserMembership.SCHEMA.id }
          ]
        }
      },
      'required': [ 'id', 'version', 'timeAdded', 'name', 'email', 'state' ]
    };

    /**
     * Use this contructor to create a new User to be persited on the server. Use {@link
     * domain.users.User.create|create()} or {@link domain.users.User.asyncCreate|asyncCreate()} to
     * create objects returned by the server.
     *
     * @class
     * @memberOf domain.users
     * @extends domain.ConcurrencySafeEntity
     *
     * @classdesc Informaiton for a user of the system.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    function User(obj) {
      /**
       * The user's full name.
       *
       * @name domain.users.User#name
       * @type {string}
       */
      this.name = '';

      /**
       * The user's email address.
       *
       * @name domain.users.User#email
       * @type {string}
       */
      this.email = '';

      /**
       * The user's optional avatar URL.
       *
       * @name domain.users.User#avatarUrl
       * @type {string}
       */

      /**
       * The roles this user has.
       *
       * @name domain.users.User#roles
       * @type {Array<string>}
       */

      /**
       * The state can be one of: registered, active or locked.
       *
       * @name domain.users.User#state
       * @type {domain.users.UserState}
       */
      this.state = UserState.REGISTERED;

      ConcurrencySafeEntity.call(this, SCHEMA, obj);
      this.membership = new UserMembership(_.get(obj, 'membership', {}));
    }

    User.prototype = Object.create(ConcurrencySafeEntity.prototype);
    User.prototype.constructor = User;

    /**
     * Checks if <tt>obj</tt> has valid properties to construct a {@link domain.users.User|User}.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
     */
    User.isValid = function (obj) {
       return ConcurrencySafeEntity.isValid(SCHEMA, [UserMembership.SCHEMA], obj);
    };

    /**
     * Creates a User, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.users.User} A user created from the given object.
     *
     * @see {@link domain.users.User.asyncCreate|asyncCreate()} when you need to create
     * a user within asynchronous code.
     */
    User.create = function (obj) {
      var validation = User.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new User(obj);
    };

    /**
     * Creates a User from a server reply, but first validates that <tt>obj</tt> has a valid schema.
     * <i>Meant to be called from within promise code.</i>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.users.User>} A user wrapped in a promise.
     *
     * @see {@link domain.users.User.create|create()} when not creating a User within asynchronous code.
     */
    User.asyncCreate = function (obj) {
      var result;

      try {
        result = User.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    };

    /**
     * Retrieves a User from the server.
     *
     * @param {string} id the ID of the user to retrieve.
     *
     * @returns {Promise<domain.users.User>} The user within a promise.
     */
    User.get = function(id) {
      return biobankApi.get(uri(id)).then(User.prototype.asyncCreate);
    };

    /**
     * Used to list users.
     *
     * @param {object} options - The options to use.
     *
     * @param {string} options.filter The filter expression to use on user to refine the list.
     *
     * @param {string} options.sor Users can be sorted by 'name', 'email' or by 'state'. Values other
     * than these yield an error. Use a minus sign prefix to sort in descending order.
     *
     * @param {int} options.page If the total results are longer than limit, then page selects which
     * users should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.limit The total number of users to return per page. The maximum page size is
     * 10. If a value larger than 10 is used then the response is an error.
     *
     * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
     * domain.users.User}.
     */
    User.list = function(options) {
      var validKeys = [ 'filter',
                        'sort',
                        'page',
                        'limit'
                      ],
          params;

      options = options || {};
      params = _.omitBy(_.pick(options, validKeys), function (value) {
        return value === '';
      });

      return biobankApi.get(uri(), params).then(function(reply) {
        // reply is a paged result
        var deferred = $q.defer();
        try {
          reply.items = _.map(reply.items, function(obj){
            return User.create(obj);
          });
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid users from server');
        }
        return deferred.promise;
      });
    };

    /**
     * Creates a User from a server reply but first validates that it has a valid schema.
     *
     * <i>A wrapper for {@link domain.users.User#asyncCreate}.</i>
     *
     * @see {@link domain.ConcurrencySafeEntity#update}
     */
    User.prototype.asyncCreate = function (obj) {
      return User.asyncCreate(obj);
    };

    User.prototype.register = function (password) {
      var json = {
        name:      this.name,
        email:     this.email,
        password:  password,
        avatarUrl: this.avatarUrl
      };
      return biobankApi.post(uri(), json)
        .then(User.prototype.asyncCreate);
    };

    User.prototype.updateName = function (name) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, updateUri('name', this.id), { name: name });
    };

    User.prototype.updateEmail = function (email) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, updateUri('email', this.id), { email: email });
    };

    User.prototype.updatePassword = function (currentPassword, newPassword) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, updateUri('password', this.id),
        {
          currentPassword: currentPassword,
          newPassword:     newPassword
        });
    };

    User.prototype.updateAvatarUrl = function (avatarUrl) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, updateUri('avatarurl', this.id), { avatarUrl: avatarUrl });
    };

    User.prototype.activate = function () {
      var self = this;

      if (self.state !== UserState.REGISTERED) {
        throw new DomainError('user state is not registered: ' + self.state);
      }

      return changeStatus(this, 'activate');
    };

    User.prototype.lock = function () {
      if ((this.state !== UserState.REGISTERED) && (this.state !== UserState.ACTIVE))  {
        throw new DomainError('user state is not registered or active: ' + this.state);
      }

      return changeStatus(this, 'lock');
    };

    User.prototype.unlock = function () {
      var self = this;

      if (self.state !== UserState.LOCKED) {
        throw new DomainError('user state is not locked: ' + self.state);
      }

      return changeStatus(this, 'unlock');
    };

    User.prototype.isRegistered = function () {
      return (this.state === UserState.REGISTERED);
    };

    User.prototype.isActive = function () {
      return (this.state === UserState.ACTIVE);
    };

    User.prototype.isLocked = function () {
      return (this.state === UserState.LOCKED);
    };

    User.prototype.hasRole = function (role) {
      return _.includes(this.roles, role);
    };

    User.prototype.hasAnyRoleOf = function (/* role1, role2, ..., roleN */) {
      return _.intersection(Array.prototype.slice.call(arguments), this.roles).length > 0;
    };

    User.prototype.hasStudyAdminRole = function () {
      return this.hasRole('StudyAdministrator');
    };

    User.prototype.hasCentreAdminRole = function () {
      return this.hasRole('CentreAdministrator');
    };

    User.prototype.hasUserAdminRole = function () {
      return this.hasRole('UserAdministrator');
    };

    function changeStatus(user, state) {
      var json = {
        id:              user.id,
        expectedVersion: user.version
      };
      return biobankApi.post(updateUri(state, user.id), json)
        .then(User.prototype.asyncCreate);
    }

    function uri(userId) {
      var result = '/users/';
      if (arguments.length > 0) {
        result += userId;
      }
      return result;
    }

    function updateUri(/* path, userId */) {
      var result = '/users/',
          args = _.toArray(arguments),
          path,
          userId;

      if (args.length > 0) {
        path = args.shift();
        result += path;
      }

      if (args.length > 0) {
        userId = args.shift();
        result += '/' + userId;
      }
      return result;
    }

    return User;
  }

  return UserFactory;
});
