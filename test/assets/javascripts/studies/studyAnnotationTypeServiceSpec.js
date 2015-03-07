/**
 * Jasmine test suite for:
 *
 *   - ceventAnnotTypesService
 *   - processingTypesService
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
      context.annotTypeUriPart       = 'ceannottypes';

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
      context.annotTypeUriPart       = 'slannottypes';

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
      context.annotTypeUriPart       = 'pannottypes';

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

      function uri(annotTypeId, version) {
        var result = '/studies/' + context.study.id + '/' + context.annotTypeUriPart;
        if (arguments.length > 0) {
          result += '/' + annotTypeId;
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
        var annotType = new AnnotationTypeType(serverAnnotTypeNoId);
        var cmd = getAddCommand(annotType);

        var expectedResult = {status: 'success', data: 'success'};
        httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);

        studyAnnotTypesService.addOrUpdate(annotType).then(function(reply) {
          expect(reply).toEqual('success');
        });
        httpBackend.flush();
      });

      it('should allow updating an annotation type', function() {
        var annotType = new AnnotationTypeType(serverAnnotType);
        var cmd = getUpdateCommand(annotType);

        var expectedResult = {status: 'success', data: 'success'};

        httpBackend.expectPUT(uri(annotType.id), cmd).respond(201, expectedResult);

        studyAnnotTypesService.addOrUpdate(annotType).then(function(reply) {
          expect(reply).toEqual('success');
        });
        httpBackend.flush();
      });

      it('should remove an annotation type', function() {
        var annotType = new AnnotationTypeType(serverAnnotType);
        httpBackend.expectDELETE(uri(annotType.id, annotType.version)).respond(201);
        studyAnnotTypesService.remove(annotType);
        httpBackend.flush();
      });

      function getAddCommand(annotType) {
        var result = _.pick(annotType, 'studyId', 'name', 'valueType', 'options');
        _.each(['description', 'maxValueCount', 'required'], function(optionalKey) {
          if (!_.isUndefined(annotType[optionalKey])) {
            result[optionalKey] = annotType[optionalKey];
          }
        });
      }

      function getUpdateCommand(annotType) {
        return _.extend(getAddCommand(annotType), { expectedVersion: annotType.version });
      }

    });

  }

});
