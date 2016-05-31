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
  'faker'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('collectionDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element([
        '<collection',
        '  study-counts="vm.counts">',
        '</collection>'
      ].join(''));

      scope.vm = { counts: test.counts };
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('collection')
      };
    }

    function getCounts() {
      return {
        disabled: faker.random.number(),
        enabled: faker.random.number(),
        retired: faker.random.number()
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q                  = self.$injector.get('$q');
      self.$rootScope          = self.$injector.get('$rootScope');
      self.$compile            = self.$injector.get('$compile');
      self.Study               = self.$injector.get('Study');
      self.factory        = self.$injector.get('factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/collection/collection.html',
        '/assets/javascripts/collection/directives/selectStudy/selectStudy.html');

      self.counts = getCounts();
    }));

    it('has valid scope', function() {
      var directive;

      spyOn(this.Study, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([])));

      directive = createDirective(this);
      expect(directive.controller.studyCounts).toBe(this.counts);

      expect(directive.controller.updateEnabledStudies).toBeFunction();
      expect(directive.controller.getEnabledStudiesPanelHeader).toBeFunction();
    });

  });

});
