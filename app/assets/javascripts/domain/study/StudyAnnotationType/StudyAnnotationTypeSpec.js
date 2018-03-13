/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import ngModule from '../../index'

describe('StudyAnnotationType', function () {
  var StudyAnnotationType;

    beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(_StudyAnnotationType_) {
      StudyAnnotationType = _StudyAnnotationType_;
    });
    });

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
