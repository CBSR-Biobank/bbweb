/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash'
import ngModule from '../../index'

describe('Permission', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function(EntityTestSuiteMixin, ServerReplyMixin) {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin)

      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              '$httpParamSerializer',
                              'Permission',
                              'EntityInfo',
                              'Factory')

      this.url = (...pathItems) => {
        const args = [ 'access/permissions' ].concat(pathItems)
        return EntityTestSuiteMixin.url.apply(null, args)
      }

      this.expectPermission = (entity) => {
        expect(entity).toEqual(jasmine.any(this.Permission));
      };
    })
  })

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation()
    this.$httpBackend.verifyNoOutstandingRequest()
  })

  it('invoking constructor with no parameters has default values', function() {
    const permission = new this.Permission();
    [ permission.parentData, permission.childData ].forEach(info => {
      expect(info).toBeArray()
    })
  })

  describe('when creating', function() {

    it('can create from JSON', function() {
      const json = this.Factory.permission(),
            permission = this.Permission.create(json);
      [ permission.parentData, permission.childData ].forEach(data => {
        expect(data).toBeNonEmptyArray();
        data.forEach(info => {
          expect(info).toEqual(jasmine.any(this.EntityInfo));
        })
      });
    });

    it('fails when required fields are missing', function() {
      const json = this.Factory.permission();
      ['id', 'version', 'timeAdded', 'parentData', 'childData'].forEach(field => {
        expect(() => {
          const badJson = _.omit(json, [field]);
          this.Permission.create(badJson);
        }).toThrowError(/:Missing required property:/);
      });
    });

  });

  describe('for creating asynchronously', function() {

    it('can create from JSON', function() {
      const json = this.Factory.permission();
      this.Permission.asyncCreate(json)
        .then(permission => {
          [ permission.parentData, permission.childData ].forEach(data => {
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
      const json = this.Factory.permission();

      ['id', 'version', 'timeAdded', 'parentData', 'childData'].forEach(field => {
        const badJson = _.omit(json, [field]);
        this.Permission.asyncCreate(badJson)
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

  describe('when getting a single permission', function() {

    it('can retrieve a single permission', function() {
      const id = this.Factory.stringNext(),
            json = this.Factory.permission()
      this.$httpBackend.whenGET(this.url(id)).respond(this.reply(json))
      this.Permission.get(id).then(this.expectPermission).catch(failTest)
      this.$httpBackend.flush()
    })

    it('fails when getting a permission and it has a bad format', function() {
      this.Permission.SCHEMA.required.forEach((property) => {
        const json = this.Factory.permission(),
              badJson = _.omit(json, property)
        this.$httpBackend.whenGET(this.url(json.id)).respond(this.reply(badJson))
        this.Permission.get(json.id).then(shouldNotFail).catch(shouldFail)
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

  // used by promise tests
  function failTest(error) {
    expect(error).toBeUndefined()
  }

})
