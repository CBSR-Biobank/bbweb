/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('StudyViewer', function() {

  var StudyViewer, Study, factory, testUtils, centre;

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(_StudyViewer_,
                                 _Study_,
                                 _testUtils_,
                                 _factory_) {
      StudyViewer = _StudyViewer_;
      Study       = _Study_;
      testUtils   = _testUtils_;
      factory     = _factory_;

      centre = factory.centre();
    });
  });

  it('should open a modal when created', function () {
    var modal = this.$injector.get('$uibModal');
    spyOn(modal, 'open').and.callFake(function () {
      return testUtils.fakeModal();
    });

    // jshint unused:false
    var study = factory.study();
    var viewer = new StudyViewer(study);

    expect(modal.open).toHaveBeenCalled();
  });

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
    study = factory.study();
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
      case 'State':
        expect(attr.value).toBe(study.state.toUpperCase());
        break;
      default:
        jasmine.getEnv().fail('label is invalid: ' + attr.label);
      }
    });
  });

});
