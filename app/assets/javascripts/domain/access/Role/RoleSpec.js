/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash'
import ngModule from '../../index'

describe('Role', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function(EntityTestSuiteMixin, ServerReplyMixin) {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin)

      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              '$httpParamSerializer',
                              'Role',
                              'EntityInfo',
                              'Factory')

      this.url = (...pathItems) => {
        const args = [ 'access/roles' ].concat(pathItems)
        return EntityTestSuiteMixin.url.apply(null, args)
      }

      this.expectRole = (entity) => {
        expect(entity).toEqual(jasmine.any(this.Role));
      };
    })
  })

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation()
    this.$httpBackend.verifyNoOutstandingRequest()
  })

  it('invoking constructor with no parameters has default values', function() {
    const role = new this.Role();
    [ role.userData, role.parentData, role.childData ].forEach(info => {
      expect(info).toBeArray()
    })
  })

  describe('when creating', function() {

    it('can create from JSON', function() {
      const json = this.Factory.role(),
            role = this.Role.create(json);
      [ role.userData, role.parentData, role.childData ].forEach(data => {
        expect(data).toBeNonEmptyArray();
        data.forEach(info => {
          expect(info).toEqual(jasmine.any(this.EntityInfo));
        })
      });
    });

    it('fails when required fields are missing', function() {
      const json = this.Factory.role();
      ['id', 'version', 'timeAdded', 'userData', 'parentData', 'childData'].forEach(field => {
        expect(() => {
          const badJson = _.omit(json, [field]);
          this.Role.create(badJson);
        }).toThrowError(/:Missing required property:/);
      });
    });

  });

  describe('for creating asynchronously', function() {

    it('can create from JSON', function() {
      const json = this.Factory.role();
      this.Role.asyncCreate(json)
        .then(role => {
          [ role.userData, role.parentData, role.childData ].forEach(data => {
            expect(data).toBeNonEmptyArray();
            data.forEach(info => {
              expect(info).toEqual(jasmine.any(this.EntityInfo));
            })
          });
        })
        .catch(err => {
          fail('should never be called: ' + err.message);
        });
      this.$rootScope.$digest();
    });

    it('fails when required fields are missing', function() {
      const json = this.Factory.role();

      ['id', 'version', 'timeAdded', 'userData', 'parentData', 'childData'].forEach(field => {
        const badJson = _.omit(json, [field]);
        this.Role.asyncCreate(badJson)
          .then(() => {
            fail('should never be called');
          })
          .catch(err => {
            expect(err.message).toContain(':Missing required property:');
          });
      });
      this.$rootScope.$digest();
    });

  })

  describe('when getting a single role', function() {

    it('can retrieve a single role', function() {
      const id = this.Factory.stringNext(),
            json = this.Factory.role()
      this.$httpBackend.whenGET(this.url(id)).respond(this.reply(json))
      this.Role.get(id).then(this.expectRole).catch(failTest)
      this.$httpBackend.flush()
    })

    it('fails when getting a role and it has a bad format', function() {
      this.Role.SCHEMA.required.forEach((property) => {
        const json = this.Factory.role(),
              badJson = _.omit(json, property)
        this.$httpBackend.whenGET(this.url(json.id)).respond(this.reply(badJson))
        this.Role.get(json.id).then(shouldNotFail).catch(shouldFail)
        this.$httpBackend.flush()
      })

      function shouldNotFail() {
        fail('function should not be called')
      }

      function shouldFail(error) {
        expect(error.message).toContain('Missing required property')
      }
    })

  })

  describe('when listing roles', function() {

    it('can retrieve', function() {
      const roles = [ this.Factory.role() ],
            reply = this.Factory.pagedResult(roles),
            testRole = (pagedResult) => {
              expect(pagedResult.items).toBeArrayOfSize(1)
              expect(pagedResult.items[0]).toEqual(jasmine.any(this.Role))
            }

      this.$httpBackend.whenGET(this.url()).respond(this.reply(reply))
      this.Role.list().then(testRole).catch(failTest)
      this.$httpBackend.flush()
    })

    it('can use options', function() {
      const optionList = [
        { filter: 'name::test' },
        { page: 2 },
        { limit: 10 }
      ],
            roles = [ this.Factory.role() ],
            reply = this.Factory.pagedResult(roles)

      optionList.forEach((options) => {
        const url = this.url() + '?' + this.$httpParamSerializer(options),
              testRole = (pagedResult) => {
                expect(pagedResult.items).toBeArrayOfSize(roles.length)
                pagedResult.items.forEach((role) => {
                  expect(role).toEqual(jasmine.any(this.Role))
                })
              }

        this.$httpBackend.whenGET(url).respond(this.reply(reply))
        this.Role.list(options).then(testRole).catch(failTest)
        this.$httpBackend.flush()
      })
    })

    it('listing omits empty options', function() {
      const options = { filter: ''},
            roles = [ this.Factory.role() ],
            reply = this.Factory.pagedResult(roles),
            testRole = (pagedResult) => {
              expect(pagedResult.items).toBeArrayOfSize(roles.length)
              pagedResult.items.forEach((role) => {
                expect(role).toEqual(jasmine.any(this.Role))
              })
            }

      this.$httpBackend.whenGET(this.url()).respond(this.reply(reply))
      this.Role.list(options).then(testRole).catch(failTest)
      this.$httpBackend.flush()
    })

    it('fails when an invalid role is returned', function() {
      const json = [ _.omit(this.Factory.role(), 'id') ],
            reply = this.Factory.pagedResult(json)

      this.$httpBackend.whenGET(this.url()).respond(this.reply(reply))
      this.Role.list().then(listFail).catch(shouldFail)
      this.$httpBackend.flush()

      function listFail() {
        fail('function should not be called')
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid roles from server')
      }
    })

  })

  it('can add a role', function() {
    const options =
          {
            userData: [ this.Factory.entityInfo() ],
            parentData: [ this.Factory.entityInfo() ],
            childData: [ this.Factory.entityInfo() ]
          },
          jsonRole = this.Factory.role(options),
          role     = this.Role.create(jsonRole),
          reqJson  = _.pick(jsonRole, 'name', 'description');

    reqJson.userIds   = jsonRole.userData.map(getEntityDataIds);
    reqJson.parentIds = jsonRole.parentData.map(getEntityDataIds);
    reqJson.childIds  = jsonRole.childData.map(getEntityDataIds);

    this.$httpBackend.expectPOST(this.url(), reqJson).respond(this.reply(jsonRole));
    role.add().then(this.expectMembership).catch(failTest);
    this.$httpBackend.flush();

    function getEntityDataIds(entityInfo) {
      return entityInfo.id;
    }
  })

  describe('when attempting to remove a role', function() {

    it('the role should be removed', function() {
      const role = this.Role.create(this.Factory.role()),
            url = this.url(role.id, role.version)

      this.$httpBackend.expectDELETE(url).respond(this.reply(true))
      role.remove()
      this.$httpBackend.flush()
    })

    it('if the role is new it cannot be removed', function() {
      const role = new this.Role()
      expect(() => {
        role.remove()
      }).toThrowError(/role has not been persisted/)
    })

  })

  it('can update the name', function() {
    const jsonRole = this.Factory.role(),
          role     = this.Role.create(jsonRole),
          name     = this.Factory.stringNext();

    this.updateEntity.call(this,
                           role,
                           'updateName',
                           name,
                           this.url('name', role.id),
                           { name: name },
                           jsonRole,
                           this.expectRole,
                           failTest);
  })

  it('can update the description', function() {
    const jsonRole    = this.Factory.role(),
          role        = this.Role.create(jsonRole),
          description = this.Factory.stringNext()

    this.updateEntity.call(this,
                           role,
                           'updateDescription',
                           undefined,
                           this.url('description', role.id),
                           { },
                           jsonRole,
                           this.expectRole,
                           failTest)

    this.updateEntity.call(this,
                           role,
                           'updateDescription',
                           description,
                           this.url('description', role.id),
                           { description: description },
                           jsonRole,
                           this.expectRole,
                           failTest)
  })

  it('can add a user', function() {
    const jsonRole = this.Factory.role(),
          role     = this.Role.create(jsonRole),
          userId   = this.Factory.stringNext()

    this.updateEntity.call(this,
                           role,
                           'addUser',
                           userId,
                           this.url('user', role.id),
                           { userId: userId },
                           jsonRole,
                           this.expectRole,
                           failTest)
  })

  describe('for user data', function() {
    const context = {}
    beforeEach(function() {
      context.entityName       = 'user'
      context.entityFieldName  = 'userData'
      context.removeEntityFunc = 'removeUser'
      context.urlPath          = 'user'
    });
    sharedEntityBehaviour(context)
  })

  it('can add a parent role', function() {
    const jsonRole     = this.Factory.role(),
          role         = this.Role.create(jsonRole),
          parentRoleId = this.Factory.stringNext()

    this.updateEntity.call(this,
                           role,
                           'addParentRole',
                           parentRoleId,
                           this.url('parent', role.id),
                           { parentRoleId: parentRoleId },
                           jsonRole,
                           this.expectRole,
                           failTest)
  })

  describe('for parent data', function() {
    const context = {}
    beforeEach(function() {
      context.entityName       = 'parent'
      context.entityFieldName  = 'parentData'
      context.removeEntityFunc = 'removeParentRole'
      context.urlPath          = 'parent'
    });
    sharedEntityBehaviour(context)
  })

  it('can add a child role', function() {
    const jsonRole     = this.Factory.role(),
          role         = this.Role.create(jsonRole),
          childRoleId = this.Factory.stringNext()

    this.updateEntity.call(this,
                           role,
                           'addChildRole',
                           childRoleId,
                           this.url('child', role.id),
                           { childRoleId: childRoleId },
                           jsonRole,
                           this.expectRole,
                           failTest)
  })

  describe('for child data', function() {
    const context = {}
    beforeEach(function() {
      context.entityName       = 'child'
      context.entityFieldName  = 'childData'
      context.removeEntityFunc = 'removeChildRole'
      context.urlPath          = 'child'
    });
    sharedEntityBehaviour(context)
  })

  function sharedEntityBehaviour(context) {

    describe('(shared) when removing an entity', function() {

      it('can remove a user', function() {
        const entityInfo = this.Factory.entityInfo(),
              entityId   = entityInfo.id,
              entityData = {}

        entityData[context.entityFieldName] =  [ entityInfo ]

        const jsonRole = this.Factory.role(entityData),
              role     = this.Role.create(jsonRole),
              url      = this.url(context.urlPath, role.id, role.version, entityId)

        this.$httpBackend.expectDELETE(url).respond(this.reply(jsonRole));
        role[context.removeEntityFunc](entityId)
          .then((reply) => {
            expect(reply).toEqual(jasmine.any(this.Role));
          })
          .catch(failTest);
        this.$httpBackend.flush();
      });

      it('cannot remove a user from a new role', function() {
        var role = new this.Role();
        expect(() => {
        role[context.removeEntityFunc]()
        }).toThrowError(/role has not been persisted/);
      });

    });

  }

  // used by promise tests
  function failTest(error) {
    expect(error).toBeUndefined()
  }

})
