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
  fdescribe('service: ceventAnnotTypesService', function () {

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
  fdescribe('service: spcLinkAnnotTypesService', function () {

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
  fdescribe('service: participantAnnotTypesService', function () {

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

      var httpBackend, studyAnnotTypesService, annotationTypeType;
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
        annotationTypeType     = context.annotationTypeType;
        study                  = context.study;
        serverAnnotType        = context.serverAnnotType;
        serverAnnotTypeNoId    = _.omit(context.serverAnnotType, 'id', 'version');
      });

      afterEach(function() {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
      });

      it('should have the following functions', function () {
        expect(studyAnnotTypesService.getAll).toBeFunction();
        expect(studyAnnotTypesService.get).toBeFunction();
        expect(studyAnnotTypesService.addOrUpdate).toBeFunction();
        expect(studyAnnotTypesService.remove).toBeFunction();
      });

      it('list should return a list containing one annotation type', function() {
        httpBackend.whenGET(uri()).respond({
          status: 'success',
          data: [serverAnnotType]
        });

        studyAnnotTypesService.getAll(study.id).then(function(annotTypes) {
          expect(annotTypes.length).toEqual(1);
          _.each(annotTypes, function(at) {
            expect(at).toEqual(serverAnnotType);
          });

        });

        httpBackend.flush();
      });

      it('get should return a valid object', function() {
        httpBackend.whenGET(uri() + '?annotTypeId=' + serverAnnotType.id).respond({
          status: 'success',
          data: serverAnnotType
        });

        studyAnnotTypesService.get(serverAnnotType.studyId, serverAnnotType.id).then(function(at) {
          expect(at).toEqual(serverAnnotType);
        });

        httpBackend.flush();
      });

      it('should allow adding an annotation type', function() {
        var annotType = new annotationTypeType(serverAnnotTypeNoId);
        var cmd = annotType.getAddCommand();

        var expectedResult = {status: 'success', data: 'success'};
        httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);

        studyAnnotTypesService.addOrUpdate(annotType).then(function(reply) {
          expect(reply).toEqual('success');
        });
        httpBackend.flush();
      });

      it('should allow updating an annotation type', function() {
        var annotType = new annotationTypeType(serverAnnotType);
        var cmd = annotType.getUpdateCommand();

        var expectedResult = {status: 'success', data: 'success'};

        httpBackend.expectPUT(uri(annotType.id), cmd).respond(201, expectedResult);

        studyAnnotTypesService.addOrUpdate(annotType).then(function(reply) {
          expect(reply).toEqual('success');
        });
        httpBackend.flush();
      });

      it('should remove an annotation type', function() {
        var annotType = new annotationTypeType(serverAnnotType);
        httpBackend.expectDELETE(uri(annotType.id, annotType.version)).respond(201);
        studyAnnotTypesService.remove(annotType);
        httpBackend.flush();
      });

    });

  }

});
