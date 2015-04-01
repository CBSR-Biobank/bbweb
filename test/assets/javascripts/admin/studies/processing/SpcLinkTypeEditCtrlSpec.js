/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: SpcLinkTypeEditCtrl', function() {
    var q,
        rootScope,
        controller,
        state,
        SpecimenLinkType,
        SpecimenLinkAnnotationType,
        domainEntityService,
        notificationsService,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               $rootScope,
                               $controller,
                               $state,
                               _SpecimenLinkType_,
                               _SpecimenLinkAnnotationType_,
                               _domainEntityService_,
                               _notificationsService_,
                               fakeDomainEntities) {
      q                          = $q;
      rootScope                  = $rootScope;
      controller                 = $controller;
      state                      = $state;
      SpecimenLinkType           = _SpecimenLinkType_;
      SpecimenLinkAnnotationType = _SpecimenLinkAnnotationType_;
      domainEntityService        = _domainEntityService_;
      notificationsService       = _notificationsService_;
      fakeEntities               = fakeDomainEntities;
    }));

    function createEntities() {
      var study, processingType, specimenGroups, annotationTypes, specimenLinkType;

      study = fakeEntities.study();
      processingType = fakeEntities.processingType(study);
      specimenGroups = _.each(_.range(2), function () {
        fakeEntities.specimenGroup(study);
      });
      annotationTypes = _.map(
          ['Text', 'Number', 'DateTime', 'Select'],
          function(valueType) {
            return new SpecimenLinkAnnotationType(
              fakeEntities.studyAnnotationType(
                study, { valueType: valueType }));
          });

      specimenLinkType = new SpecimenLinkType(fakeEntities.create);
      specimenLinkType.studySpecimenGroups(specimenGroups);
      specimenLinkType.studyAnnotationTypes(annotationTypes);

      return {
        study:            study,
        processingType:   processingType,
        specimenGroups:   specimenGroups,
        annotationTypes:  annotationTypes,
        specimenLinkType: specimenLinkType
      };
    }

    function createController(entities) {
      var scope = rootScope.$new();

      controller('SpcLinkTypeEditCtrl as vm', {
        $scope:               scope,
        $state:               state,
        SpecimenLinkType:     SpecimenLinkType,
        domainEntityService:  domainEntityService,
        notificationsService: notificationsService,
        study:                entities.study,
        spcLinkType:          entities.specimenLinkType,
        processingDto:        {
          processingTypes:             [ entities.processingType ],
          specimenLinkTypes:           [ entities.specimenLinkType ],
          specimenLinkAnnotationTypes: entities.annotationTypes,
          specimenGroups:              entities.specimenGroups
        }
      });

      scope.$digest();
      return scope;
    }

    describe('has valid scope when created', function () {

      it('for new specimen link type', function() {
        var entities = createEntities({ noCetId: true }),
            scope = createController(entities);
      });

      it('for existing specimen link type', function() {
        var entities = createEntities(),
            scope = createController(entities);
        jasmine.getEnv().fail();
      });

    });

    it('can submit a specimen link type', function() {
      jasmine.getEnv().fail();
    });

    it('on submit error, displays an error modal', function() {
      jasmine.getEnv().fail();
    });

    it('when user presses the cancel button, goes to correct state', function() {
      jasmine.getEnv().fail();
    });

    it('can add annotation type', function() {
      jasmine.getEnv().fail();
    });

    it('can remove annotation type', function() {
      jasmine.getEnv().fail();
    });

    it('removing an annotation type with invalid index throws an error', function() {
      jasmine.getEnv().fail();
    });

    it('getSpecimenGroupUnits returns valid results', function() {
      jasmine.getEnv().fail();
    });

  });

});
