/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../../test/entityNameSharedBehaviour';

describe('StudyName', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(EntityTestSuite,
                                 ServerReplyMixin) {
      _.extend(this, EntityTestSuite.prototype, ServerReplyMixin.prototype);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'EntityName',
                              'StudyName',
                              'StudyState',
                              'factory');

      //this.testUtils.addCustomMatchers();
      this.jsonStudyName = this.factory.studyNameDto();

      // used by promise tests
      this.expectStudy = (entity) => {
        expect(entity).toEqual(jasmine.any(this.StudyName));
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'studies/names' ].concat(_.toArray(arguments));
        return EntityTestSuite.prototype.url.apply(null, args);
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
      context.constructor = this.StudyName;
      context.createFunc  = this.StudyName.create;
      context.restApiUrl  = this.url();
      context.factoryFunc = this.factory.studyNameDto;
      context.listFunc    = this.StudyName.list;
    });

    sharedBehaviour(context);

  });

  it('state predicates return valid results', function() {
    _.values(this.StudyState).forEach((state) => {
      var studyName = this.StudyName.create(this.factory.studyNameDto({ state: state }));
      expect(studyName.isDisabled()).toBe(state === this.StudyState.DISABLED);
      expect(studyName.isEnabled()).toBe(state === this.StudyState.ENABLED);
      expect(studyName.isRetired()).toBe(state === this.StudyState.RETIRED);
    });
  });

});
