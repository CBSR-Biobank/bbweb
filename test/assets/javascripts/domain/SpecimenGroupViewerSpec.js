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

  describe('SpecimenGroupViewer', function() {

    var SpecimenGroupViewer, fakeEntities, centre;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_SpecimenGroupViewer_,
                               fakeDomainEntities) {
      SpecimenGroupViewer = _SpecimenGroupViewer_;
      fakeEntities   = fakeDomainEntities;

      centre = fakeEntities.centre();
    }));

    it('should open a modal when created', function() {
      var count = 0,
          modal = this.$injector.get('$modal'),
          study,
          specimenGroup,
          viewer;

      spyOn(modal, 'open').and.callFake(function () { return testUtils.fakeModal(); });

      // jshint unused:false
      study = fakeEntities.study();
      specimenGroup = fakeEntities.specimenGroup(study);
      viewer = new SpecimenGroupViewer(specimenGroup);

      expect(modal.open).toHaveBeenCalled();
    });

    it('should display valid attributes', function() {
      var EntityViewer = this.$injector.get('EntityViewer');
      var attributes, study, specimenGroup, viewer;

      spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
        attributes.push({label: label, value: value});
      });

      attributes = [];
      study = fakeEntities.study();
      specimenGroup = fakeEntities.specimenGroup(study);
      viewer = new SpecimenGroupViewer(specimenGroup);

      expect(attributes).toBeArrayOfSize(7);

      _.each(attributes, function(attr) {
        switch (attr.label) {
        case 'Name':
          expect(attr.value).toBe(specimenGroup.name);
          break;
        case 'Units':
          expect(attr.value).toBe(specimenGroup.units);
          break;
        case 'Anatomical Source':
          expect(attr.value).toBe(specimenGroup.anatomicalSourceType);
          break;
        case 'Preservation Type':
          expect(attr.value).toBe(specimenGroup.preservationType);
          break;
        case 'Preservation Temperature':
          expect(attr.value).toBe(specimenGroup.preservationTemperatureType);
          break;
        case 'Specimen Type':
          expect(attr.value).toBe(specimenGroup.specimenType);
          break;
        case 'Description':
          expect(attr.value).toBe(specimenGroup.description);
          break;
        default:
          jasmine.getEnv().fail('label is invalid: ' + attr.label);
        }
      });
    });

  });

});
