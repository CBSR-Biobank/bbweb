/**
 * Jasmine test suite for:
 *
 *   - ceventAnnotTypesService
 *   - participantAnnotTypesService
 *   - spcLinkAnnotTypesService
 *
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp',
  'biobankTest'
], function(angular, mocks, _) {
  'use strict';

  /**
   * Suite for ceventAnnotTypesService
   */
  describe('service: ceventAnnotTypesService', function () {

    var context = {};

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($httpBackend,
                                fakeDomainEntities,
                                extendedDomainEntities,
                                CollectionEventAnnotationType,
                                ceventAnnotTypesService) {
      var fakeEntities = fakeDomainEntities;

      context.httpBackend            = $httpBackend;
      context.annotationTypeType     = CollectionEventAnnotationType;
      context.studyAnnotTypesService = ceventAnnotTypesService;
      context.study                  = fakeEntities.study();
      context.annotationTypeUriPart       = 'ceannottypes';

      //context.serverAnnotType = fakeEntities.studyAnnotationType(context.study, { required: false });
      context.serverAnnotType = fakeEntities.studyAnnotationType(context.study);
    }));

    sharedBehaviourForStudyAnnotationTypes(context);
  });

  /**
   * Suite for spcLinkAnnotTypesService
   */
  describe('service: spcLinkAnnotTypesService', function () {

    var context = {};

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($httpBackend,
                                fakeDomainEntities,
                                extendedDomainEntities,
                                SpecimenLinkAnnotationType,
                                spcLinkAnnotTypesService) {
      var fakeEntities = fakeDomainEntities;

      context.httpBackend            = $httpBackend;
      context.annotationTypeType     = SpecimenLinkAnnotationType;
      context.studyAnnotTypesService = spcLinkAnnotTypesService;
      context.study                  = fakeEntities.study();
      context.annotationTypeUriPart       = 'slannottypes';

      //context.serverAnnotType = fakeEntities.studyAnnotationType(context.study, { required: false });
      context.serverAnnotType = fakeEntities.studyAnnotationType(context.study);
    }));

    sharedBehaviourForStudyAnnotationTypes(context);
  });

  /**
   * Suite for participantAnnotTypesService
   */
  describe('service: participantAnnotTypesService', function () {

    var context = {};

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($httpBackend,
                                fakeDomainEntities,
                                extendedDomainEntities,
                                ParticipantAnnotationType,
                                participantAnnotTypesService) {
      var fakeEntities = fakeDomainEntities;

      context.httpBackend            = $httpBackend;
      context.annotationTypeType     = ParticipantAnnotationType;
      context.studyAnnotTypesService = participantAnnotTypesService;
      context.study                  = fakeEntities.study();
      context.annotationTypeUriPart       = 'pannottypes';

      context.serverAnnotType = fakeEntities.studyAnnotationType(context.study, { required: true });
    }));

    sharedBehaviourForStudyAnnotationTypes(context);
  });

  /**
   * Shared spec for all 3 services listed above.
   */
  function sharedBehaviourForStudyAnnotationTypes(context) {

    describe('(shared)', function() {

      var httpBackend, studyAnnotTypesService, AnnotationTypeType;
      var study, serverAnnotType, serverAnnotTypeNoId;

      function uri(annotationTypeId, version) {
        var result = '/studies/' + context.study.id + '/' + context.annotationTypeUriPart;
        if (arguments.length > 0) {
          result += '/' + annotationTypeId;
        }
        if (arguments.length > 1) {
          result += '/' + version;
        }
        return result;
      }

      beforeEach(function () {
        httpBackend            = context.httpBackend;
        studyAnnotTypesService = context.studyAnnotTypesService;
        AnnotationTypeType     = context.annotationTypeType;
        study                  = context.study;
        serverAnnotType        = context.serverAnnotType;
        serverAnnotTypeNoId    = _.omit(context.serverAnnotType, 'id', 'version');
      });

      afterEach(function() {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
      });

      it('should have the following functions', function () {
        expect(studyAnnotTypesService.addOrUpdate).toBeFunction();
        expect(studyAnnotTypesService.remove).toBeFunction();
      });

      it('should allow adding an annotation type', function() {
        var annotationType = new AnnotationTypeType(serverAnnotTypeNoId);
        var cmd = getAddCommand(annotationType);

        var expectedResult = {status: 'success', data: 'success'};
        httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);

        studyAnnotTypesService.addOrUpdate(annotationType).then(function(reply) {
          expect(reply).toEqual('success');
        });
        httpBackend.flush();
      });

      it('should allow updating an annotation type', function() {
        var annotationType = new AnnotationTypeType(serverAnnotType);
        var cmd = getUpdateCommand(annotationType);

        var expectedResult = {status: 'success', data: 'success'};

        httpBackend.expectPUT(uri(annotationType.id), cmd).respond(201, expectedResult);

        studyAnnotTypesService.addOrUpdate(annotationType).then(function(reply) {
          expect(reply).toEqual('success');
        });
        httpBackend.flush();
      });

      it('should remove an annotation type', function() {
        var annotationType = new AnnotationTypeType(serverAnnotType);
        httpBackend.expectDELETE(uri(annotationType.id, annotationType.version)).respond(201);
        studyAnnotTypesService.remove(annotationType);
        httpBackend.flush();
      });

      function getAddCommand(annotationType) {
        var result = _.pick(annotationType, 'studyId', 'name', 'valueType', 'options');
        _.each(['description', 'maxValueCount', 'required'], function(optionalKey) {
          if (!_.isUndefined(annotationType[optionalKey])) {
            result[optionalKey] = annotationType[optionalKey];
          }
        });
      }

      function getUpdateCommand(annotationType) {
        return _.extend(getAddCommand(annotationType), { expectedVersion: annotationType.version });
      }

    });

  }

});
