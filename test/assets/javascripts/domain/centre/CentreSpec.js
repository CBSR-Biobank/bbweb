/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Centre', function() {

    var Centre, centresService, centreLocationsSevice;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_Centre_,
                               _centresService_,
                               _centreLocationsSevice_) {
      Centre                = _Centre_;
      centresService        = _centresService_;
      centreLocationsSevice = _centreLocationsSevice_;
    }));

    it('constructor with no parameters has null ID', function() {
      var centre = new Centre();
      expect(centre.id).toBeNull();

    });


  });

});
