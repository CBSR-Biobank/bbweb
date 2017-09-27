/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      sharedBehaviour = require('../../test/entityNameSharedBehaviour');

  describe('UserName', function() {

    var REST_API_URL = '/users/names';

    function SuiteMixinFactory(EntityTestSuite, ServerReplyMixin) {

      function SuiteMixin() {
        EntityTestSuite.call(this);
        ServerReplyMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(EntityTestSuite.prototype);
      _.extend(SuiteMixin.prototype, ServerReplyMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      // used by promise tests
      SuiteMixin.prototype.expectUser = function (entity) {
        expect(entity).toEqual(jasmine.any(this.UserName));
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuite,
                               ServerReplyMixin,
                               testDomainEntities) {
      var SuiteMixin = new SuiteMixinFactory(EntityTestSuite, ServerReplyMixin);

      _.extend(this, SuiteMixin.prototype);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityName',
                              'UserName',
                              'UserState',
                              'factory');

      //this.testUtils.addCustomMatchers();
      this.jsonUserName = this.factory.userNameDto();
      testDomainEntities.extend();
    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });

    describe('common behaviour', function() {

      var context = {};

      beforeEach(function() {
        context.constructor = this.UserName;
        context.createFunc  = this.UserName.create;
        context.restApiUrl  = REST_API_URL;
        context.factoryFunc = this.factory.userNameDto;
        context.listFunc    = this.UserName.list;
      });

      sharedBehaviour(context);

    });

    it('state predicates return valid results', function() {
      var self = this;
      _.each(_.values(self.UserState), function(state) {
        var userName = self.UserName.create(self.factory.userNameDto({ state: state }));
        expect(userName.isRegistered()).toBe(state === self.UserState.REGISTERED);
        expect(userName.isActive()).toBe(state === self.UserState.ACTIVE);
        expect(userName.isLocked()).toBe(state === self.UserState.LOCKED);
      });
    });

  });

});
