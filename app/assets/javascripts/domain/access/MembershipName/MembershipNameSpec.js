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

describe('MembershipName', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin)

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityInfo',
                              'MembershipName',
                              'Factory')
      this.url = () =>  EntityTestSuiteMixin.url('access/memberships/names')
    })
  })

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation()
    this.$httpBackend.verifyNoOutstandingRequest()
  })

  describe('create behaviour', function() {

    var context = {}

    beforeEach(function() {
      context.constructor = this.MembershipName
      context.createFunc  = this.MembershipName.create
      context.restApiUrl  = this.url
      context.jsonFactoryFunc = () => this.Factory.membershipNameDto()
      context.listFunc = (options) => this.MembershipName.list(options)
    })

    entityInfoCreateSharedBehaviour(context)

  })

  describe('list behaviour', function() {

    var context = {}

    beforeEach(function() {
      context.constructor = this.MembershipName
      context.restApiUrl  = this.url
      context.jsonFactoryFunc = () => this.Factory.membershipNameDto()
      context.listFunc = (options) => this.MembershipName.list(options)
    })

    entityInfoListSharedBehaviour(context)

  })

})
