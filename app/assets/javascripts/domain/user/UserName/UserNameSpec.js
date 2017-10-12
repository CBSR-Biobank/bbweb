/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../../test/entityNameSharedBehaviour';

describe('UserName', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(EntityTestSuite,
                                 ServerReplyMixin) {

      _.extend(this, EntityTestSuite, ServerReplyMixin.prototype);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityName',
                              'UserName',
                              'UserState',
                              'Factory');

      //this.TestUtils.addCustomMatchers();
      this.jsonUserName = this.Factory.userNameDto();

      this.expectUser = (entity) => {
        expect(entity).toEqual(jasmine.any(this.UserName));
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'users/names' ].concat(_.toArray(arguments));
        return EntityTestSuite.url.apply(null, args);
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
    _.values(self.UserState).forEach((state) => {
      var userName = self.UserName.create(self.Factory.userNameDto({ state: state }));
      expect(userName.isRegistered()).toBe(state === self.UserState.REGISTERED);
      expect(userName.isActive()).toBe(state === self.UserState.ACTIVE);
      expect(userName.isLocked()).toBe(state === self.UserState.LOCKED);
    });
  });

});
