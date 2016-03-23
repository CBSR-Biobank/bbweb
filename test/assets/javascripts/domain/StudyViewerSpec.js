/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('StudyViewer', function() {

    var StudyViewer, Study, jsonEntities, centre;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_StudyViewer_,
                               _Study_,
                               _jsonEntities_) {
      StudyViewer    = _StudyViewer_;
      Study          = _Study_;
      jsonEntities   = _jsonEntities_;

      centre = jsonEntities.centre();
    }));

    it('should open a modal when created', inject(function (testUtils) {
      var count = 0;
      var modal = this.$injector.get('$uibModal');
      spyOn(modal, 'open').and.callFake(function () {
        return testUtils.fakeModal();
      });

      // jshint unused:false
      var study = jsonEntities.study();
      var viewer = new StudyViewer(study);

      expect(modal.open).toHaveBeenCalled();
    }));

    it('should display valid attributes', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          $filter = this.$injector.get('$filter'),
          attributes,
          study,
          viewer;

      spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
        attributes.push({label: label, value: value});
      });

      attributes = [];
      study = jsonEntities.study();
      viewer = new StudyViewer(study);

      expect(attributes).toBeArrayOfSize(3);

      _.each(attributes, function(attr) {
        switch (attr.label) {
        case 'Name':
          expect(attr.value).toBe(study.name);
          break;
        case 'Description':
          expect(attr.value).toBe($filter('truncate')(study.description, 60));
          break;
        case 'Status':
          expect(attr.value).toBe(study.statusLabel);
          break;
        default:
          jasmine.getEnv().fail('label is invalid: ' + attr.label);
        }
      });
    });

  });

});
