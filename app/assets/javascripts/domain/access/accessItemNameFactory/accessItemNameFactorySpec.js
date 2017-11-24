/**
 * Jasmine test suite
 *
 */
/* global angular */

import * as sharedBehaviour from '../../../test/behaviours/entityNameSharedBehaviour'
import ngModule from '../../index'

describe('accessItemNameFactory', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function(EntityTestSuiteMixin, ServerReplyMixin) {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin)

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'accessItemNameFactory',
                              'RoleName',
                              'PermissionName',
                              'Factory')
      this.url = () =>  EntityTestSuiteMixin.url('access/items/names')

      this.roleAccessItemJson =
        () => Object.assign(this.Factory.roleNameDto(),
                           { accessItemType: 'role' })

      this.permissionAccessItemJson =
        () => Object.assign(this.Factory.permissionNameDto(),
                           { accessItemType: 'permission' })
    })
  })

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation()
    this.$httpBackend.verifyNoOutstandingRequest()
  })

  describe('list behaviour', function() {

    var context = {}

    beforeEach(function() {
      context.constructor = this.PermissionName
      context.restApiUrl = this.url
      context.jsonFactoryFunc = this.permissionAccessItemJson
      context.listFunc = (options) => this.accessItemNameFactory.list(options)
    })

    sharedBehaviour.entityNameListSharedBehaviour(context)

  })

  describe('for list', function() {

    it('can handle roles and permissions', function() {
      const names = [ this.roleAccessItemJson(), this.permissionAccessItemJson() ],
            testEntity = reply => {
              expect(reply).toBeArrayOfSize(names.length)
              expect(reply[0]).toEqual(jasmine.any(this.RoleName))
              expect(reply[1]).toEqual(jasmine.any(this.PermissionName))
            }

      this.$httpBackend.whenGET(this.url()).respond(this.reply(names))
      this.accessItemNameFactory.list().then(testEntity).catch(this.failTest)
      this.$httpBackend.flush()
    })

  })

})
