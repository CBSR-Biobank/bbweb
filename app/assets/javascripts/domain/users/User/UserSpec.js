/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('User', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              'User',
                              'UserState',
                              'Factory');

      // used by promise tests
      this.expectUser = (entity) => {
        expect(entity).toEqual(jasmine.any(this.User));
      };

      this.url = (...paths) => {
        const allPaths = [ 'users' ].concat(paths);
        return EntityTestSuiteMixin.url(...allPaths);
      }
    });
  });

  it('creating a user with no parameters has default values', function() {
    const user = new this.User();
    expect(user.id).toBeNull();
    expect(user.version).toBe(0);
    expect(user.timeAdded).toBeUndefined();
    expect(user.timeModified).toBeUndefined();
    expect(user.name).toBeUndefined();
    expect(user.email).toBeUndefined();
    expect(user.state).toBe(this.UserState.REGISTERED);
  });

  it('creating a user with an object does not modify object', function() {
    const obj = null,
          user = new this.User(obj);
    expect(user).toBeObject();
    expect(obj).toBeNull();
  });

  it('fails when creating from object with a non object', function() {
    const nonObj = 1;
    expect(() => {
      this.User.create(nonObj);
    }).toThrowError(/Invalid type/);
  });

  it('fails when creating from object with missing required keys', function() {
    const obj = this.Factory.user(),
          requiredKeys = ['id', 'name', 'email', 'state'];

    requiredKeys.forEach((key) => {
      const badObj = _.omit(obj, key);

      expect(() => {
        this.User.create(badObj);
      }).toThrowError(/Missing required property/);
    });
  });

  describe('when listing multiple users', function() {

    it('can list using when filtering by name only', function() {
      const filter = 'name::test',
            url = this.url('search') + '?filter=' + filter,
            rawUser = this.Factory.user(),
            reply = this.Factory.pagedResult([ rawUser ]);

      this.$httpBackend.whenGET(url).respond(this.reply(reply));

      this.User.list({ filter: filter })
        .then((pagedResult) => {
          expect(pagedResult.items).toBeArrayOfSize(1);
          expect(pagedResult.items[0]).toEqual(jasmine.any(this.User));
        })
        .catch(error => {
          fail('here' + error);
        });
      this.$httpBackend.flush();
    });

    it('can list when using filtering by email only', function() {
      const filter = 'email::test',
            url = this.url('search') + '?filter=' + filter,
            rawUser = this.Factory.user(),
            reply = this.Factory.pagedResult([ rawUser ]);

      this.$httpBackend.whenGET(url).respond(this.reply(reply));

      this.User.list({ filter: filter }).then((pagedResult) => {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(this.User));
      });
      this.$httpBackend.flush();
    });

    it('can list using sort parameter only', function() {
      const sort = 'name',
            url = this.url('search') + '?sort=' + sort,
            rawUser = this.Factory.user(),
            reply = this.Factory.pagedResult([ rawUser ]);

      this.$httpBackend.whenGET(url).respond(this.reply(reply));

      this.User.list({ sort: sort }).then((pagedResult) => {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(this.User));
      });
      this.$httpBackend.flush();
    });

    it('should query for multiple users', function() {
      const filter   = 'email::test',
            sort     = '-email',
            url      = this.url('search') + `?filter=${filter}&sort=${sort}`,
            rawUser = this.Factory.user(),
            reply    = this.Factory.pagedResult([ rawUser ]);

      this.$httpBackend.whenGET(url).respond(this.reply(reply));

      this.User.list({ filter: filter, sort: sort }).then((pagedResult) => {
        expect(pagedResult.items).toBeArrayOfSize(1);
        expect(pagedResult.items[0]).toEqual(jasmine.any(this.User));
      });
      this.$httpBackend.flush();
    });

    it('should handle an invalid response', function() {
      const reply = this.Factory.pagedResult([ { 'a': 1 } ]);

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
    const user = this.Factory.user();

    this.$httpBackend.whenGET(this.url(user.slug)).respond(this.reply(user));
    this.User.get(user.slug).then((reply) => {
      expect(reply).toEqual(jasmine.any(this.User));
    });
    this.$httpBackend.flush();
  });

  it('can register a user', function() {
    const password   = this.Factory.stringNext(),
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
    const rawUser = this.Factory.user(),
          user = this.User.create(rawUser);

    this.updateEntity(user,
                      'updateName',
                      user.name,
                      this.url('update', user.id),
                      { property: 'name', newValue: user.name },
                      rawUser,
                      this.expectUser.bind(this),
                      failTest.bind(this));
  });

  it('can update a users email', function() {
    const rawUser = this.Factory.user(),
          user = this.User.create(rawUser);

    this.updateEntity(user,
                      'updateEmail',
                      user.email,
                      this.url('update', user.id),
                      { property: 'email', newValue: user.email },
                      rawUser,
                      this.expectUser.bind(this),
                      failTest.bind(this));
  });

  it('can update a users avatar url', function() {
    const newAvatarUrl = this.Factory.stringNext(),
          rawUser = this.Factory.user({ avatarUrl: newAvatarUrl }),
          user = this.User.create(rawUser);

    this.updateEntity(user,
                      'updateAvatarUrl',
                      user.avatarUrl,
                      this.url('update', user.id),
                      { property: 'avatarUrl', newValue: user.avatarUrl },
                      rawUser,
                      this.expectUser.bind(this),
                      failTest.bind(this));
  });

  it('can update a users password', function() {
    const currentPassword = this.Factory.stringNext(),
          newPassword = this.Factory.stringNext(),
          rawUser = this.Factory.user(),
          user = this.User.create(rawUser);

    this.updateEntity(user,
                      'updatePassword',
                      [ currentPassword, newPassword ],
                      this.url('update', user.id),
                      {
                        property: 'password',
                        newValue: {
                          currentPassword: currentPassword,
                          newPassword:     newPassword
                        }
                      },
                      rawUser,
                      this.expectUser.bind(this),
                      failTest.bind(this));
  });

  it('can change a user`s state', function() {
    const stateData = [
      { initialState: this.UserState.REGISTERED, action: 'activate' },
      { initialState: this.UserState.ACTIVE,     action: 'lock' },
      { initialState: this.UserState.LOCKED,     action: 'unlock' }
    ];

    stateData.forEach(stateInfo => {
      const rawUser = this.Factory.user({ state: stateInfo.initialState }),
            user = this.User.create(rawUser);

      this.updateEntity(user,
                        stateInfo.action,
                        null,
                        this.url('update', user.id),
                        { property: 'state', newValue: stateInfo.action },
                        rawUser,
                        this.expectUser.bind(this),
                        failTest.bind(this));
    });
  });

  it('fails when calling activate and the user is not registered', function() {
    const stateData = [
      {
        action: 'activate',
        initialStates: [ this.UserState.ACTIVE, this.UserState.LOCKED ]
      },
      {
        action: 'lock',
        initialStates: [ this.UserState.LOCKED ]
      },
      {
        action: 'unlock',
        initialStates: [ this.UserState.REGISTERED, this.UserState.ACTIVE ]
      }
    ];

    stateData.forEach((stateInfo) => {
      stateInfo.initialStates.forEach(initialState => {
        const user = this.User.create(this.Factory.user({ state: initialState }));

        expect(() => { user[stateInfo.action](); })
          .toThrowError(/user state is not/);
      });
    });
  });

  it('state predicates are valid valid', function() {
    Object.values(this.UserState).forEach((state) => {
      const rawUser = this.Factory.user({ state: state }),
            user     = new this.User(rawUser);

      expect(user.isRegistered()).toBe(state === this.UserState.REGISTERED);
      expect(user.isActive()).toBe(state === this.UserState.ACTIVE);
      expect(user.isLocked()).toBe(state === this.UserState.LOCKED);
    });
  });

  describe('when adding a role', function() {

    it('uses correct URL when adding a role', function() {
      const rawUser = this.Factory.user(),
            rawRole  = this.Factory.role(),
            user     = new this.User(rawUser),
            reqJson = {
              expectedVersion: user.version,
              roleId:          rawRole.id
            };

      this.$httpBackend.expectPOST(this.url('roles', user.id), reqJson)
        .respond(this.reply(rawUser));

      user.addRole(rawRole.id).then((reply) => {
        expect(reply).toEqual(jasmine.any(this.User));
      });
      this.$httpBackend.flush();

    });

    it('throws an error when adding a role the user already has', function() {
      const rawUserRole  = this.Factory.userRole(),
            rawUser = this.Factory.user({ roles: [ rawUserRole ]}),
            user     = new this.User(rawUser);

      expect(() => {
        user.addRole(rawUserRole.id).then(this.expectUser.bind(this));
      }).toThrowError(/user already has role/);
    })

  })

  describe('when removing a role', function() {

    it('uses correct URL when adding a role', function() {
      const rawUserRole  = this.Factory.userRole(),
            rawUser = this.Factory.user({ roles: [ rawUserRole ]}),
            user     = new this.User(rawUser);

      this.$httpBackend.expectDELETE(this.url('roles', user.id, user.version, rawUserRole.id))
        .respond(this.reply(rawUser));

      user.removeRole(rawUserRole.id).then(this.expectUser.bind(this));
      this.$httpBackend.flush();
    });

    it('throws an error when removing a role the user does not have', function() {
      const rawUserRole  = this.Factory.userRole(),
            rawUser = this.Factory.user(),
            user     = new this.User(rawUser);

      user.removeRole(rawUserRole.id)
        .then(failTest)
        .catch(error => {
          expect(error).not.toBeUndefined();
        });
      this.$rootScope.$digest();
    });

  });

  function registerCommand(user, password) {
    return Object.assign(_.pick(user, 'name', 'email', 'avatarUrl'), { password: password || '' });
  }

  // used by promise tests
  function failTest(error) {
    expect(error).toBeUndefined();
  }

});
