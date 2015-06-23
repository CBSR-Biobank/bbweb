/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('StudyAnnotationType', function () {
    var StudyAnnotationType;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_StudyAnnotationType_) {
      StudyAnnotationType = _StudyAnnotationType_;
    }));

    it('addOrUpdate throws error when sub class has bad initialization', function() {
      var studyAnnotationType = new StudyAnnotationType();
      expect(function () { studyAnnotationType.addOrUpdate(); })
        .toThrow(new Error('_service is null'));
    });

    it('remove throws error when sub class has bad initialization', function() {
      var studyAnnotationType = new StudyAnnotationType();
      expect(function () { studyAnnotationType.remove(); })
        .toThrow(new Error('_service is null'));
    });

  });


});
