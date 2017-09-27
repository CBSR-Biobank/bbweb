/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('ProcessingTypeViewer', function() {

  var ProcessingTypeViewer, factory, centre;

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(_ProcessingTypeViewer_,
                                 _factory_) {
      ProcessingTypeViewer = _ProcessingTypeViewer_;
      factory   = _factory_;

      centre = factory.centre();
    });
  });

  it('should open a modal when created', inject(function (testUtils) {
    var modal = this.$injector.get('$uibModal'),
        study, processingType, viewer;

    spyOn(modal, 'open').and.callFake(function () {
      return testUtils.fakeModal();
    });

    // jshint unused:false
    study = factory.study();
    processingType = factory.processingType(study);
    viewer = new ProcessingTypeViewer(processingType);

    expect(modal.open).toHaveBeenCalled();
  }));

  it('should display valid attributes', function() {
    var EntityViewer = this.$injector.get('EntityViewer');
    var attributes, study, processingType, viewer;

    spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
      attributes.push({label: label, value: value});
    });

    study = factory.study();

    _.each([true, false], function (enabled) {
      attributes = [];
      processingType = factory.processingType(study);
      processingType.enabled = enabled;
      viewer = new ProcessingTypeViewer(processingType);

      expect(attributes).toBeArrayOfSize(3);

      _.each(attributes, function(attr) {
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
