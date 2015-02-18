/**
 * Jasmine test suite
 *
 * global define
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker',
  'biobank.fakeDomainEntities',
  'biobank.annotationTypeDataSetCommon',
  'biobankApp'
], function(angular, mocks, _, faker, fakeEntities, commonTests) {
  'use strict';

  describe('SpecimenLinkType', function() {

    var SpecimenLinkType, SpecimenGroupSet, AnnotationTypeSet, sltFromServer;

    var study = fakeEntities.study();
    var processingType = fakeEntities.processingType(study);

    study.specimenGroups = _.map(_.range(2), function() {
      return fakeEntities.specimenGroup(study);
    });

    study.annotationTypes = _.map(_.range(2), function() {
      return fakeEntities.annotationType(study);
    });

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function(_SpecimenLinkType_, _SpecimenGroupSet_, _AnnotationTypeSet_) {
      SpecimenLinkType = _SpecimenLinkType_;
      SpecimenGroupSet    = _SpecimenGroupSet_;
      AnnotationTypeSet   = _AnnotationTypeSet_;

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

    it('should be initialized with AnnotationTypeSet', function() {
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
          studySpecimenGroupSet: new SpecimenGroupSet(study.specimenGroups),
          studyAnnotationTypeSet: new AnnotationTypeSet(study.annotationTypes)
        });

      expect(slt.inputGroup).toEqual(study.specimenGroups[0]);
      expect(slt.outputGroup).toEqual(study.specimenGroups[1]);

      _.each(study.annotationTypes, function(at) {
        expect(slt.getAnnotationTypeData(at.id).annotationType).toEqual(at);
      });
    });

    it('calling addAnnotationTypeData adds a new annotation type data item', function() {
      var slt = new SpecimenLinkType(study,
                                     sltFromServer,
                                     { studyAnnotationTypes: study.annotationTypes });
      var newAtDataItem = fakeEntities.annotationTypeData(study.annotationTypes[0]);
      commonTests.addItem(slt, newAtDataItem, study.annotationTypes[0]);
    });


    it('allows adding multiple annotation type data items with empty id', function() {
      var slt = new SpecimenLinkType(study,
                                     sltFromServer,
                                     { studyAnnotationTypes: study.annotationTypes });
      var noIdAtDataItem = {annotationTypeId: '', required: false};
      commonTests.addItem(slt, noIdAtDataItem);
    });

    it('allow removing annotation type data items', function() {
      var sltFromServer = fakeEntities.specimenLinkType(
        processingType,
        { annotationTypes: study.annotationTypes }
      );
      var slt = new SpecimenLinkType(study,
                                     sltFromServer,
                                     { studyAnnotationTypes: study.annotationTypes });
      commonTests.removeItems(slt);
    });

    it('returns annotation type data as a string', function() {
      var sltFromServer;

      var annotationTypes = _.map(_.range(2), function() {
        return fakeEntities.annotationType(study);
      });

      sltFromServer = fakeEntities.specimenLinkType(processingType, { annotationTypes: annotationTypes});
      sltFromServer.annotationTypeData[0].required = true;
      sltFromServer.annotationTypeData[0].required = false;

      var slt = new SpecimenLinkType(study,
                                     sltFromServer,
                                     { studyAnnotationTypes: annotationTypes });
      commonTests.getAsString(slt);
    });


  });

});
