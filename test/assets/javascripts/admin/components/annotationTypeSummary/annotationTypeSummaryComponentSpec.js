/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('annotationTypeSummaryComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);

      self.putHtmlTemplates(
        '/assets/javascripts/admin/components/annotationTypeSummary/annotationTypeSummary.html');

      self.injectDependencies('$componentController',
                              'AnnotationType',
                              'factory');
    }));

    it('can be created', function () {
      this.$componentController(
        'annotationTypeSummary',
        null,
        {
          annotationType: new this.AnnotationType(this.factory.annotationType()),
          test: 'xxx'
        });
    });

  });

});
