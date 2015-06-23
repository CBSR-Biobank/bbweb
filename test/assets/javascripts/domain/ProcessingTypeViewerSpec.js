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
  'biobank.testUtils',
  'biobankApp'
], function(angular,
            mocks,
            _,
            testUtils) {
  'use strict';

  describe('ProcessingTypeViewer', function() {

    var ProcessingTypeViewer, fakeEntities, centre;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_ProcessingTypeViewer_,
                               fakeDomainEntities) {
      ProcessingTypeViewer = _ProcessingTypeViewer_;
      fakeEntities   = fakeDomainEntities;

      centre = fakeEntities.centre();
    }));

    it('should open a modal when created', function() {
      var modal = this.$injector.get('$modal'),
          study, processingType, viewer;

      spyOn(modal, 'open').and.callFake(function () { return testUtils.fakeModal(); });

      // jshint unused:false
      study = fakeEntities.study();
      processingType = fakeEntities.processingType(study);
      viewer = new ProcessingTypeViewer(processingType);

      expect(modal.open).toHaveBeenCalled();
    });

    it('should display valid attributes', function() {
      var EntityViewer = this.$injector.get('EntityViewer');
      var attributes, study, processingType, viewer;

      spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
        attributes.push({label: label, value: value});
      });

      study = fakeEntities.study();

      _.each([true, false], function (enabled) {
        attributes = [];
        processingType = fakeEntities.processingType(study);
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

});
