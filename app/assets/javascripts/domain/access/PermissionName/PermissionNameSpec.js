/**
 * Jasmine test suite
 *
 */
/* global angular */

import * as sharedBehaviour from '../../../test/behaviours/entityNameSharedBehaviour'
import ngModule from '../../index'

describe('PermissionName', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function(EntityTestSuiteMixin, ServerReplyMixin) {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin)

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityName',
                              'PermissionName',
                              'Factory')
      this.url = () =>  EntityTestSuiteMixin.url('access/permissions/names')
    })
  })

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation()
    this.$httpBackend.verifyNoOutstandingRequest()
  })

  describe('create behaviour', function() {

    var context = {}

    beforeEach(function() {
      context.constructor = this.PermissionName
      context.createFunc  = this.PermissionName.create
      context.restApiUrl  = this.url
      context.jsonFactoryFunc = () => this.Factory.permissionNameDto()
      context.listFunc = (options) => this.PermissionName.list(options)
    })

    sharedBehaviour.entityNameCreateSharedBehaviour(context)

  })

})
