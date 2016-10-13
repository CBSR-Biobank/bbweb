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

  SuiteMixin.prototype.createDirective = function (specimenSpec) {
    specimenSpec = specimenSpec || this.specimenSpec;
    this.element = angular.element([
      '<collection-specimen-spec-summary',
      '  specimen-spec="vm.specimenSpec">',
      '</collection-specimen-spec-summary>'
    ].join(''));

    this.scope = this.$rootScope.$new();
    this.scope.vm = { specimenSpec: specimenSpec };
    this.$compile(this.element)(this.scope);
    this.scope.$digest();
  };

  describe('collectionSpecimenSpecSummaryDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(factory, TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype, SuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              'CollectionSpecimenSpec');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenSpecSummary/collectionSpecimenSpecSummary.html');

      self.specimenSpec = new self.CollectionSpecimenSpec(factory.collectionSpecimenSpec());

    }));

    it('can be created', function() {
      this.createDirective();
      expect(this.scope.vm.specimenSpec).toBe(this.specimenSpec);
    });


  });

});
