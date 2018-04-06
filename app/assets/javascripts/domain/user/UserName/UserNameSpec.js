/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import _ from 'lodash';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/entityNameAndStateSharedBehaviour';

describe('UserName', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {

      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityNameAndState',
                              'UserName',
                              'UserState',
                              'Factory');

      //this.addCustomMatchers();
      this.jsonUserName = this.Factory.userNameDto();

      this.expectUser = (entity) => {
        expect(entity).toEqual(jasmine.any(this.UserName));
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'users/names' ].concat(_.toArray(arguments));
        return EntityTestSuiteMixin.url.apply(null, args);
      }
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  describe('common behaviour', function() {

    var context = {};

    beforeEach(function() {
      context.constructor = this.UserName;
      context.createFunc  = this.UserName.create;
      context.restApiUrl  = this.url();
      context.factoryFunc = this.Factory.userNameDto.bind(this.Factory);
      context.listFunc    = this.UserName.list;
    });

    sharedBehaviour(context);

  });

  it('state predicates return valid results', function() {
    var self = this;
    Object.values(self.UserState).forEach((state) => {
      var userName = self.UserName.create(self.Factory.userNameDto({ state: state }));
      expect(userName.isRegistered()).toBe(state === self.UserState.REGISTERED);
      expect(userName.isActive()).toBe(state === self.UserState.ACTIVE);
      expect(userName.isLocked()).toBe(state === self.UserState.LOCKED);
    });
  });

});
