// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular,
            mocks,
            _,
            testUtils) {
  'use strict';

  describe('SpcLinkTypeViewer', function() {

    var SpcLinkTypeViewer, SpecimenLinkType, fakeEntities, centre;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_SpcLinkTypeViewer_,
                               _SpecimenLinkType_,
                               fakeDomainEntities) {
      SpcLinkTypeViewer = _SpcLinkTypeViewer_;
      SpecimenLinkType       = _SpecimenLinkType_;
      fakeEntities   = fakeDomainEntities;

      centre = fakeEntities.centre();
    }));

    function createSpecimenLinkType() {
      var study = fakeEntities.study();
      var processingType = fakeEntities.processingType(study);
      var specimenGroups = [
        fakeEntities.specimenGroup(study),
        fakeEntities.specimenGroup(study),
      ];
      var annotationTypes = [
        fakeEntities.annotationType(),
        fakeEntities.annotationType()
      ];

      var baseSpcLinkType = fakeEntities.specimenLinkType(processingType, {
        inputGroup: specimenGroups[0],
        outputGroup: specimenGroups[1],
        annotationTypes: annotationTypes
      });

      return new SpecimenLinkType(processingType, baseSpcLinkType, {
        studySpecimenGroups: specimenGroups,
        studyAnnotationTypes: annotationTypes
      });
    }

    it('should open a modal when created', function() {
      var count = 0,
          modal = this.$injector.get('$modal'),
          specimenLinkType, viewer;

      spyOn(modal, 'open').and.callFake(function () { return testUtils.fakeModal(); });

      // jshint unused:fals
      specimenLinkType = createSpecimenLinkType();
      viewer = new SpcLinkTypeViewer(specimenLinkType);

      expect(modal.open).toHaveBeenCalled();
    });

    it('should display valid attributes', function() {
      var EntityViewer = this.$injector.get('EntityViewer');
      var attributes, specimenLinkType, viewer;

      spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
        attributes.push({label: label, value: value});
      });

      attributes = [];
      specimenLinkType = createSpecimenLinkType();
      viewer = new SpcLinkTypeViewer(specimenLinkType);

      expect(attributes).toBeArrayOfSize(10);

      _.each(attributes, function(attr) {
        switch (attr.label) {
        case 'Processing Type':
          expect(attr.value).toBe(specimenLinkType.processingType.name);
          break;
        case 'Input Group':
          expect(attr.value).toBe(specimenLinkType.inputGroup.name);
          break;
        case 'Expected input change':
          expect(attr.value).toBe(specimenLinkType.expectedInputChange + ' ' + specimenLinkType.inputGroup.units);
          break;
        case 'Input count':
          expect(attr.value).toBe(specimenLinkType.inputCount);
          break;
        case 'Input Container Type':
          expect(attr.value).toBe('None');
          break;
        case 'Output Group':
          expect(attr.value).toBe(specimenLinkType.outputGroup.name);
          break;
        case 'Expected output change':
          expect(attr.value).toBe(specimenLinkType.expectedInputChange + ' ' + specimenLinkType.outputGroup.units);
          break;
        case 'Output count':
          expect(attr.value).toBe(specimenLinkType.outputCount);
          break;
        case 'Output Container Type':
          expect(attr.value).toBe('None');
          break;
        case 'Annotation Types':
          expect(attr.value).toBe(specimenLinkType.getAnnotationTypesAsString());
          break;
        default:
          jasmine.getEnv().fail('label is invalid: ' + attr.label);
        }
      });
    });

  });

});
