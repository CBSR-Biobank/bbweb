/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  function SuiteMixin() {}

  SuiteMixin.prototype.createDirective = function (specimenDescription) {
    specimenDescription = specimenDescription || this.specimenDescription;
    this.element = angular.element([
      '<collection-specimen-description-summary',
      '  specimen-description="vm.specimenDescription">',
      '</collection-specimen-description-summary>'
    ].join(''));

    this.scope = this.$rootScope.$new();
    this.scope.vm = { specimenDescription: specimenDescription };
    this.$compile(this.element)(this.scope);
    this.scope.$digest();
  };

  describe('collectionSpecimenDescriptionSummaryDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(factory, TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype, SuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionSpecimenDescription');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenDescriptionSummary/collectionSpecimenDescriptionSummary.html');

      self.specimenDescription = new self.CollectionSpecimenDescription(factory.collectionSpecimenDescription());

    }));

    it('can be created', function() {
      this.createDirective();
      expect(this.scope.vm.specimenDescription).toBe(this.specimenDescription);
    });


  });

});
