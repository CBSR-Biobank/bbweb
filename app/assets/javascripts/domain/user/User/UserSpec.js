/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('User', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(EntityTestSuite, ServerReplyMixin) {
      _.extend(this, EntityTestSuite.prototype, ServerReplyMixin.prototype);

      this.injectDependencies('$httpBackend',
                              'User',
                              'UserState',
                              'Factory');

      // used by promise tests
      this.expectUser = (entity) => {
        expect(entity).toEqual(jasmine.any(this.User));
      };

      this.statusChangeShared = (jsonObj, user, statusChangePath, userMethod) => {
        var json = { id: user.id, expectedVersion: user.version };

        this.$httpBackend.expectPOST(this.url(statusChangePath, user.id), json)
          .respond(this.reply(jsonObj));

        user[userMethod]().then((replyUser) => {
          expect(replyUser).toEqual(jasmine.any(this.User));
        });
        this.$httpBackend.flush();
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'users' ].concat(_.toArray(arguments));
        return EntityTestSuite.prototype.url.apply(null, args);
      }
    });
  });

  it('creating a user with no parameters has default values', function() {
    var user = new this.User();
    expect(user.id).toBeNull();
    expect(user.version).toBe(0);
    expect(user.timeAdded).toBeNull();
    expect(user.timeModified).toBeNull();
    expect(user.name).toBeEmptyString();
    expect(user.email).toBeEmptyString();
    expect(user.state).toBe(this.UserState.REGISTERED);
  });

  it('creating a user with an object does not modify object', function() {
    var obj = null, user = new this.User(obj);
    expect(user).toBeObject();
    expect(obj).toBeNull();
  });

  it('fails when creating from object with a non object', function() {
    var nonObj = 1;
    expect(() => {
      this.User.create(nonObj);
    }).toThrowError(/Invalid type/);
  });

  it('fails when creating from object with missing required keys', function() {
    var obj = this.Factory.user(),
        requiredKeys = ['id', 'name', 'email', 'state'];

    requiredKeys.forEach((key) => {
      var badObj = _.omit(obj, key);

      expect(() => {
        this.User.create(badObj);
      }).toThrowError(/Missing required property/);
    });
  });

  describe('when listing multiple users', function() {

    it('can list using when filtering by name only', function() {
      var filter = 'name::test',
          url = this.url('search') + '?filter=' + filter,
          jsonUser = this.Factory.user(),
          reply = this.Factory.pagedResult([ jsonUser ]);

      this.$httpBackend.whenGET(url).respond(this.reply(reply));

      this.User.list({ filter: filter }).then((pagedResult) => {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(this.User));
      });
      this.$httpBackend.flush();
    });

    it('can list when using filtering by email only', function() {
      var filter = 'email::test',
          url = this.url('search') + '?filter=' + filter,
          jsonUser = this.Factory.user(),
          reply = this.Factory.pagedResult([ jsonUser ]);

      this.$httpBackend.whenGET(url).respond(this.reply(reply));

      this.User.list({ filter: filter }).then((pagedResult) => {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(this.User));
      });
      this.$httpBackend.flush();
    });

    it('can list using sort parameter only', function() {
      var sort = 'name',
          url = this.url('search') + '?sort=' + sort,
          jsonUser = this.Factory.user(),
          reply = this.Factory.pagedResult([ jsonUser ]);

      this.$httpBackend.whenGET(url).respond(this.reply(reply));

      this.User.list({ sort: sort }).then((pagedResult) => {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(this.User));
      });
      this.$httpBackend.flush();
    });

    it('should query for multiple users', function() {
      var filter   = 'email::test',
          sort     = '-email',
          url      = this.url('search') + `?filter=${filter}&sort=${sort}`,
          jsonUser = this.Factory.user(),
          reply    = this.Factory.pagedResult([ jsonUser ]);

      this.$httpBackend.whenGET(url).respond(this.reply(reply));

      this.User.list({ filter: filter, sort: sort }).then((pagedResult) => {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(this.User));
      });
      this.$httpBackend.flush();
    });

    it('should handle an invalid response', function() {
      var reply = this.Factory.pagedResult([ { 'a': 1 } ]);

      this.$httpBackend.whenGET(this.url('search')).respond(this.reply(reply));

      this.User.list().then(testFail).catch(checkError);
      this.$httpBackend.flush();

      function testFail() {
        fail('should not be called');
      }

      function checkError(error) {
        expect(error).toContain('invalid users from server');
      }
    });

  });

  it('can retrieve a single user', function() {
    var user = this.Factory.user();

    this.$httpBackend.whenGET(this.url(user.id)).respond(this.reply(user));
    this.User.get(user.id).then((reply) => {
      expect(reply).toEqual(jasmine.any(this.User));
    });
    this.$httpBackend.flush();
  });

  it('can register a user', function() {
    var password   = this.Factory.stringNext(),
        serverUser = this.Factory.user(),
        user       = new this.User(_.omit(serverUser, 'id')),
        cmd        = registerCommand(user, password);

    this.$httpBackend.expectPOST(this.url(), cmd).respond(this.reply(serverUser));

    user.register(password).then((reply) => {
      expect(reply).toEqual(jasmine.any(this.User));
    });
    this.$httpBackend.flush();
  });

  it('can update a users name', function() {
    var jsonUser = this.Factory.user(),
        user = new this.User(jsonUser);

    this.updateEntity.call(this,
                           user,
                           'updateName',
                           user.name,
                           this.url('name', user.id),
                           { name: user.name },
                           jsonUser,
                           this.expectUser,
                           failTest);
  });

  it('can update a users email', function() {
    var jsonUser = this.Factory.user(),
        user = new this.User(jsonUser);

    this.updateEntity.call(this,
                           user,
                           'updateEmail',
                           user.email,
                           this.url('email', user.id),
                           { email: user.email },
                           jsonUser,
                           this.expectUser,
                           failTest);
  });

  it('can update a users avatar url', function() {
    var newAvatarUrl = this.Factory.stringNext(),
        jsonUser = this.Factory.user({ avatarUrl: newAvatarUrl }),
        user = new this.User(jsonUser);

    this.updateEntity.call(this,
                           user,
                           'updateAvatarUrl',
                           user.avatarUrl,
                           this.url('avatarurl', user.id),
                           { avatarUrl: user.avatarUrl },
                           jsonUser,
                           this.expectUser,
                           failTest);
  });

  it('can update a users password', function() {
    var currentPassword = this.Factory.stringNext(),
        newPassword = this.Factory.stringNext(),
        jsonUser = this.Factory.user(),
        user = new this.User(jsonUser);

    this.updateEntity.call(this,
                           user,
                           'updatePassword',
                           [ currentPassword, newPassword ],
                           this.url('password', user.id),
                           {
                             currentPassword: currentPassword,
                             newPassword:     newPassword
                           },
                           jsonUser,
                           this.expectUser,
                           failTest);
  });

  it('can activate a registered user', function() {
    var jsonObj = this.Factory.user(),
        user = this.User.create(jsonObj);
    this.statusChangeShared(jsonObj, user, 'activate', 'activate', this.UserState.ACTIVE);
  });

  it('can lock an active user', function() {
    var jsonObj = _.extend(this.Factory.user(), { state: this.UserState.ACTIVE }),
        user = new this.User(jsonObj);
    this.statusChangeShared(jsonObj, user, 'lock', 'lock', this.UserState.LOCKED);
  });

  it('can lock an REGISTERED user', function() {
    var jsonObj = _.extend(this.Factory.user(), { state: this.UserState.REGISTERED }),
        user = new this.User(jsonObj);
    this.statusChangeShared(jsonObj, user, 'lock', 'lock', this.UserState.LOCKED);
  });

  it('can unlock a locked user', function() {
    var jsonObj = _.extend(this.Factory.user(), { state: this.UserState.LOCKED }),
        user = new this.User(jsonObj);
    this.statusChangeShared(jsonObj, user, 'unlock', 'unlock', this.UserState.ACTIVE);
  });

  it('fails when calling activate and the user is not registered', function() {
    var statuses = [ this.UserState.ACTIVE, this.UserState.LOCKED ];

    statuses.forEach((state) => {
      var user = new this.User(_.extend(this.Factory.user(), { state: state }));

      expect(() => { user.activate(); })
        .toThrow(new Error('user state is not registered: ' + state));
    });
  });

  it('fails when calling lock and the user is LOCKED', function() {
    var statuses = [ this.UserState.LOCKED ];

    statuses.forEach((state) => {
      var user = new this.User(_.extend(this.Factory.user(), { state: state }));

      expect(() => { user.lock(); })
        .toThrowError(/user state is not registered or active/);
    });
  });

  it('fails when calling unlock and the user is not locked', function() {
    var statuses = [ this.UserState.REGISTERED, this.UserState.ACTIVE ];

    statuses.forEach((state) => {
      var user = new this.User(_.extend(this.Factory.user(), { state: state }));

      expect(() => { user.unlock(); })
        .toThrow(new Error('user state is not locked: ' + state));
    });
  });

  it('state predicates are valid valid', function() {
    _.values(this.UserState).forEach((state) => {
      var jsonUser = this.Factory.user({ state: state }),
          user     = new this.User(jsonUser);

      expect(user.isRegistered()).toBe(state === this.UserState.REGISTERED);
      expect(user.isActive()).toBe(state === this.UserState.ACTIVE);
      expect(user.isLocked()).toBe(state === this.UserState.LOCKED);
    });
  });

  function registerCommand(user, password) {
    return _.extend(_.pick(user, 'name', 'email', 'avatarUrl'), { password: password || '' });
  }

  // used by promise tests
  function failTest(error) {
    expect(error).toBeUndefined();
  }

});
