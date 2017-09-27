define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      sharedBehaviour = require('../../test/entityNameSharedBehaviour');

  describe('CentreName', function() {

    var REST_API_URL = '/centres/names';

    function SuiteMixinFactory(EntityTestSuite, ServerReplyMixin) {

      function SuiteMixin() {
        EntityTestSuite.call(this);
        ServerReplyMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(EntityTestSuite.prototype);
      _.extend(SuiteMixin.prototype, ServerReplyMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      // used by promise tests
      SuiteMixin.prototype.expectCentre = function (entity) {
        expect(entity).toEqual(jasmine.any(this.CentreName));
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
                              'CentreName',
                              'CentreState',
                              'factory');

      //this.testUtils.addCustomMatchers();
      testDomainEntities.extend();
    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });

    describe('common behaviour', function() {

      var context = {};

      beforeEach(function() {
        context.constructor = this.CentreName;
        context.createFunc  = this.CentreName.create;
        context.restApiUrl  = REST_API_URL;
        context.factoryFunc = this.factory.centreNameDto;
        context.listFunc    = this.CentreName.list;
      });

      sharedBehaviour(context);

    });

    it('state predicates return valid results', function() {
      var self = this;
      _.each(_.values(self.CentreState), function(state) {
        var entityName = self.CentreName.create(self.factory.centreNameDto({ state: state }));
        expect(entityName.isDisabled()).toBe(state === self.CentreState.DISABLED);
        expect(entityName.isEnabled()).toBe(state === self.CentreState.ENABLED);
      });
    });

  });

});
