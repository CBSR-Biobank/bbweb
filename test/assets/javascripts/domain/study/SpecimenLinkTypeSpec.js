/**
 * Jasmine test suite
 *
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  '../annotationTypeDataSetSharedSpec',
  'biobankApp'
], function(angular, mocks, _, annotationTypeDataSetSharedSpec) {
  'use strict';

  describe('SpecimenLinkType', function() {

    var SpecimenLinkType,
        sltFromServer,
        fakeEntities,
        study,
        processingType;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_SpecimenLinkType_,
                               fakeDomainEntities) {
      SpecimenLinkType = _SpecimenLinkType_;
      fakeEntities        = fakeDomainEntities;

      study = fakeEntities.study();
      processingType = fakeEntities.processingType(study);

      study.specimenGroups = _.map(_.range(2), function() {
        return fakeEntities.specimenGroup(study);
      });

      study.annotationTypes = _.map(_.range(2), function() {
        return fakeEntities.annotationType(study);
      });

      sltFromServer = fakeEntities.specimenLinkType(processingType);
    }));

    it('isNew should be true for a specimen link type with no ID', function() {
      var sltNoId = _.omit(sltFromServer, 'id', 'processingTypeId');
      var slt = new SpecimenLinkType(processingType, sltNoId);
      expect(slt.isNew).toBe(true);
    });

    it('processing type ID matches', function() {
      var slt = new SpecimenLinkType(processingType, sltFromServer);
      expect(slt.processingTypeId).toBe(processingType.id);
    });

    it('processing type matches', function() {
      var slt = new SpecimenLinkType(processingType, sltFromServer);
      expect(slt.processingType).toEqual(processingType);
    });

    it('isNew should be false for a specimen link type that has an ID', function() {
      var slt = new SpecimenLinkType(processingType, sltFromServer);
      expect(slt.isNew).toBe(false);
    });

    it('should be initialized with annotation type objects', function() {
      var sltFromServer = fakeEntities.specimenLinkType(
        processingType,
        {
          inputGroup: study.specimenGroups[0],
          outputGroup: study.specimenGroups[1],
          annotationTypes: study.annotationTypes
        }
      );

      var slt = new SpecimenLinkType(
        study,
        sltFromServer,
        {
          studySpecimenGroups: study.specimenGroups,
          studyAnnotationTypes: study.annotationTypes
        });

      expect(slt.inputGroup).toEqual(study.specimenGroups[0]);
      expect(slt.outputGroup).toEqual(study.specimenGroups[1]);

      _.each(study.annotationTypes, function(at) {
        expect(slt.getAnnotationTypeData(at.id).annotationType).toEqual(at);
      });
    });

    it('getServerSpecimenLinkType returns valid object', function() {
      var sltFromServer = fakeEntities.specimenLinkType(
        processingType,
        {
          inputGroup: study.specimenGroups[0],
          outputGroup: study.specimenGroups[1],
          annotationTypes: study.annotationTypes
        }
      );
      var slt = new SpecimenLinkType(study, sltFromServer, {
          studySpecimenGroups: study.specimenGroups,
          studyAnnotationTypes: study.annotationTypes
      });

      // slt should have annotation types to exercise all functionaltity
      expect(slt.annotationTypeDataSize()).toBeGreaterThan(0);

      expect(slt.getServerSpecimenLinkType()).toEqual(sltFromServer);
    });

    it('getAnnotationTypeData throws an error if there are no annotation type data items', function() {
      var slt = new SpecimenLinkType(study, sltFromServer);
      expect(function () { slt.getAnnotationTypeData(study.annotationTypes[0].id); })
        .toThrow(new Error('no data items'));
    });

    describe('uses annotation type set correctly', function () {

      var study, processingType, annotationTypes, sltFromServer, slt;
      var context = {};

      beforeEach(inject(function(SpecimenLinkType,
                                 fakeDomainEntities) {

        study = fakeDomainEntities.study();
        processingType = fakeDomainEntities.processingType(study);
        annotationTypes = _.map(_.range(2), function() {
          return fakeDomainEntities.annotationType(study);
        });

        sltFromServer = fakeDomainEntities.specimenLinkType(
          processingType,
          { annotationTypes: annotationTypes});

        sltFromServer.annotationTypeData[0].required = true;
        sltFromServer.annotationTypeData[0].required = false;

        slt = new SpecimenLinkType(study,
                                   sltFromServer,
                                   { studyAnnotationTypes: annotationTypes });
        context.parentObj = slt;
      }));

      annotationTypeDataSetSharedSpec(context);
    });


  });

});
