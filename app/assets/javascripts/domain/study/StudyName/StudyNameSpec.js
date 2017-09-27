/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      sharedBehaviour = require('../../test/entityNameSharedBehaviour');

  describe('StudyName', function() {

    var REST_API_URL = '/studies/names';

    function SuiteMixinFactory(EntityTestSuite, ServerReplyMixin) {

      function SuiteMixin() {
        EntityTestSuite.call(this);
        ServerReplyMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(EntityTestSuite.prototype);
      _.extend(SuiteMixin.prototype, ServerReplyMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      // used by promise tests
      SuiteMixin.prototype.expectStudy = function (entity) {
        expect(entity).toEqual(jasmine.any(this.StudyName));
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
                              'StudyName',
                              'StudyState',
                              'factory');

      //this.testUtils.addCustomMatchers();
      this.jsonStudyName = this.factory.studyNameDto();
      testDomainEntities.extend();
    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });

    describe('common behaviour', function() {

      var context = {};

      beforeEach(function() {
        context.constructor = this.StudyName;
        context.createFunc  = this.StudyName.create;
        context.restApiUrl  = REST_API_URL;
        context.factoryFunc = this.factory.studyNameDto;
        context.listFunc    = this.StudyName.list;
      });

      sharedBehaviour(context);

    });

    it('state predicates return valid results', function() {
      var self = this;
      _.each(_.values(self.StudyState), function(state) {
        var studyName = self.StudyName.create(self.factory.studyNameDto({ state: state }));
        expect(studyName.isDisabled()).toBe(state === self.StudyState.DISABLED);
        expect(studyName.isEnabled()).toBe(state === self.StudyState.ENABLED);
        expect(studyName.isRetired()).toBe(state === self.StudyState.RETIRED);
      });
    });

  });

});
