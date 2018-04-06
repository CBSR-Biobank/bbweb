/**
 * Jasmine test suite
 *
 */
/* global angular */

import { entityInfoCreateSharedBehaviour,
         entityInfoListSharedBehaviour } from 'test/behaviours/entityInfoSharedBehaviour';
import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import ngModule from '../../index';

describe('RoleName', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin)

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityInfo',
                              'RoleName',
                              'Factory')
      this.url = () =>  EntityTestSuiteMixin.url('access/roles/names')
    })
  })

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation()
    this.$httpBackend.verifyNoOutstandingRequest()
  })

  describe('create behaviour', function() {

    var context = {}

    beforeEach(function() {
      context.constructor = this.RoleName
      context.createFunc  = this.RoleName.create
      context.restApiUrl  = this.url
      context.jsonFactoryFunc = () => this.Factory.roleNameDto()
      context.listFunc = (options) => this.RoleName.list(options)
    })

    entityInfoCreateSharedBehaviour(context)

  })

  describe('list behaviour', function() {

    var context = {}

    beforeEach(function() {
      context.constructor = this.RoleName
      context.restApiUrl  = this.url
      context.jsonFactoryFunc = () => this.Factory.roleNameDto()
      context.listFunc = (options) => this.RoleName.list(options)
    })

    entityInfoListSharedBehaviour(context)

  })

})
