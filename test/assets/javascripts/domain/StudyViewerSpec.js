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

  describe('StudyViewer', function() {

    var StudyViewer, Study, fakeEntities, centre;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_StudyViewer_,
                               _Study_,
                               fakeDomainEntities) {
      StudyViewer = _StudyViewer_;
      Study       = _Study_;
      fakeEntities   = fakeDomainEntities;

      centre = fakeEntities.centre();
    }));

    it('should open a modal when created', function() {
      var count = 0;
      var modal = this.$injector.get('$modal');
      spyOn(modal, 'open').and.callFake(function () { return testUtils.fakeModal(); });

      // jshint unused:false
      var study = fakeEntities.study();
      var viewer = new StudyViewer(study);

      expect(modal.open).toHaveBeenCalled();
    });

    it('should display valid attributes', function() {
      var EntityViewer = this.$injector.get('EntityViewer');
      var attributes, study, viewer;

      spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
        attributes.push({label: label, value: value});
      });

      attributes = [];
      study = fakeEntities.study();
      viewer = new StudyViewer(study);

      expect(attributes).toBeArrayOfSize(3);

      _.each(attributes, function(attr) {
        switch (attr.label) {
        case 'Name':
          expect(attr.value).toBe(study.name);
          break;
        case 'Description':
          expect(attr.value).toBe(study.description);
          break;
        case 'Status':
          expect(attr.value).toBe(study.status);
          break;
        default:
          jasmine.getEnv().fail('label is invalid: ' + attr.label);
        }
      });
    });

  });

});
