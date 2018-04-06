/**
 * Jasmine test suite
 *
 */
/* global angular */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import { entityInfoCreateSharedBehaviour } from 'test/behaviours/entityInfoSharedBehaviour';
import ngModule from '../../index'

describe('PermissionName', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin)

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityInfo',
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

    entityInfoCreateSharedBehaviour(context)

  })

})
