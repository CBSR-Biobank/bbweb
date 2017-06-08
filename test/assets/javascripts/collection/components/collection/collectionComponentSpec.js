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

  describe('collectionDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this;

      _.extend(this, TestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/collection/components/collection/collection.html',
        '/assets/javascripts/collection/directives/selectStudy/selectStudy.html');

      this.createController = function () {
        self.element = angular.element('<collection></collection>');
        self.scope = self.$rootScope.$new();
        self.$compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('collection');
      };
    }));

    it('has valid scope', function() {
      spyOn(this.Study, 'collectionStudies').and.returnValue(this.$q.when(this.factory.pagedResult([])));
      this.createController();
      expect(this.controller.isCollectionAllowed).toBe(false);
      expect(this.controller.updateEnabledStudies).toBeFunction();
    });

    it('has valid scope when collections are allowed', function() {
      var study = this.factory.study();
      spyOn(this.Study, 'collectionStudies')
        .and.returnValue(this.$q.when(this.factory.pagedResult([study])));
      this.createController();
      expect(this.controller.isCollectionAllowed).toBe(true);
      expect(this.controller.updateEnabledStudies).toBeFunction();
    });

    it('studies are reloaded', function() {
      var callsCount;

      this.Study.collectionStudies = jasmine.createSpy()
        .and.returnValue(this.$q.when(this.factory.pagedResult([])));

      this.createController();
      callsCount = this.Study.collectionStudies.calls.count();

      this.controller.updateEnabledStudies();
      this.scope.$digest();
      expect(this.Study.collectionStudies.calls.count()).toBe(callsCount + 1);
    });

  });

});
