/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

xdescribe('ProcessingTypeViewer', function() {

  var ProcessingTypeViewer, Factory, TestUtils;

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(_ProcessingTypeViewer_,
                                 _Factory_,
                                 _TestUtils_) {
      ProcessingTypeViewer = _ProcessingTypeViewer_;
      Factory   = _Factory_;
      TestUtils = _TestUtils_;
    });
  });

  it('should open a modal when created', function () {
    var modal = this.$injector.get('$uibModal'),
        study,
        processingType,
        viewer;              // eslint-disable-line no-unused-vars

    spyOn(modal, 'open').and.callFake(function () {
      return TestUtils.fakeModal();
    });

    // jshint unused:false
    study = Factory.study();
    processingType = Factory.processingType(study);
    viewer = new ProcessingTypeViewer(processingType);

    expect(modal.open).toHaveBeenCalled();
  });

  it('should display valid attributes', function() {
    var EntityViewer = this.$injector.get('EntityViewer'),
        attributes,
        study,
        processingType,
        viewer;              // eslint-disable-line no-unused-vars

    spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
      attributes.push({label: label, value: value});
    });

    study = Factory.study();

    _.each([true, false], function (enabled) {
      attributes = [];
      processingType = Factory.processingType(study);
      processingType.enabled = enabled;
      viewer = new ProcessingTypeViewer(processingType);

      expect(attributes).toBeArrayOfSize(3);

      attributes.forEach((attr) => {
        switch (attr.label) {
        case 'Name':
          expect(attr.value).toBe(processingType.name);
          break;
        case 'Description':
          expect(attr.value).toBe(processingType.description);
          break;
        case 'Enabled':
          expect(attr.value).toBe(processingType.enabled ? 'Yes' : 'No');
          break;
        default:
          jasmine.getEnv().fail('label is invalid: ' + attr.label);
        }
      });

    });
  });

});
