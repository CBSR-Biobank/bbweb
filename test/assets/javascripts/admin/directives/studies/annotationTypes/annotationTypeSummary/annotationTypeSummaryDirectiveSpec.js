/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: annotationTypeSummaryDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (directiveTestSuite, testUtils, jsonEntities, AnnotationType) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/annotationTypes/annotationTypeSummary/annotationTypeSummary.html');

      self.annotationType = new AnnotationType(jsonEntities.annotationType());
    }));

    it('can be created', inject(function ($rootScope, $compile) {
      this.element = angular.element(
        '<annotation-type-summary annotation-type="vm.study"></annotation-type-summary>');
      this.scope = $rootScope.$new();
      this.scope.annotationType = this.annotationType;

      $compile(this.element)(this.scope);
      this.scope.$digest();
    }));


  });

});
