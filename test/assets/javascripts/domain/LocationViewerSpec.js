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

  describe('LocationViewer', function() {

    var LocationViewer, Location, fakeEntities, centre;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_LocationViewer_,
                               _Location_,
                               fakeDomainEntities) {
      LocationViewer = _LocationViewer_;
      Location       = _Location_;
      fakeEntities   = fakeDomainEntities;

      centre = fakeEntities.centre();
    }));

    it('should open a modal when created', inject(function(testUtils) {
      var count = 0;
      var modal = this.$injector.get('$uibModal');
      spyOn(modal, 'open').and.callFake(function () { return testUtils.fakeModal(); });

      // jshint unused:false
      var location = fakeEntities.location();
      var viewer = new LocationViewer(centre, location);

      expect(modal.open).toHaveBeenCalled();
    }));

    it('should display valid attributes', function() {
      var EntityViewer = this.$injector.get('EntityViewer');
      var attributes, location, viewer;

      spyOn(EntityViewer.prototype, 'addAttribute').and.callFake(function (label, value) {
        attributes.push({label: label, value: value});
      });

      attributes = [];
      location = fakeEntities.location();
      viewer = new LocationViewer(location);

      expect(attributes).toBeArrayOfSize(7);

      _.each(attributes, function(attr) {
        switch (attr.label) {
        case 'Name':
          expect(attr.value).toBe(location.name);
          break;
        case 'Street':
          expect(attr.value).toBe(location.street);
          break;
        case 'City':
          expect(attr.value).toBe(location.city);
          break;
        case 'Province / State':
          expect(attr.value).toBe(location.province);
          break;
        case 'Postal / Zip code':
          expect(attr.value).toBe(location.postalCode);
          break;
        case 'PO Box Number':
          expect(attr.value).toBe(location.poBoxNumber);
          break;
        case 'Country ISO Code':
          expect(attr.value).toBe(location.countryIsoCode);
          break;
        default:
          jasmine.getEnv().fail('label is invalid: ' + attr.label);
        }
      });
    });

  });

});
