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

    function getStudyCounts() {
      return {
        disabled: faker.random.number(),
        enabled: faker.random.number(),
        retired: faker.random.number()
      };
    }

    function getCentreCounts() {
      return {
        disabled: faker.random.number(),
        enabled: faker.random.number()
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$q                  = self.$injector.get('$q');
      self.Study               = self.$injector.get('Study');
      self.factory        = self.$injector.get('factory');

      self.putHtmlTemplates(
        '/assets/javascripts/collection/directives/collection/collection.html',
        '/assets/javascripts/collection/directives/selectStudy/selectStudy.html');

      self.createDirective = createDirective;

      //--

      function createDirective(studyCounts, centreCounts) {
        studyCounts = studyCounts || getStudyCounts();
        centreCounts = centreCounts || getCentreCounts();

        self.element = angular.element([
          '<collection',
          '  study-counts="vm.studyCounts"',
          '  centre-counts="vm.centreCounts">',
          '</collection>'
        ].join(''));

        self.scope = $rootScope.$new();
        self.scope.vm = {
          studyCounts: studyCounts,
          centreCounts: centreCounts
        };
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('collection');
      }

    }));

    it('has valid scope', function() {
      var studyCounts = getStudyCounts(),
          centreCounts = getCentreCounts();

      spyOn(this.Study, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([])));

      this.createDirective(studyCounts, centreCounts);
      expect(this.controller.studyCounts).toBe(studyCounts);
      expect(this.controller.centreCounts).toBe(centreCounts);
      expect(this.controller.haveEnabledStudies).toBe(studyCounts.enabled > 0);
      expect(this.controller.haveEnabledCentres).toBe(centreCounts.enabled > 0);
      expect(this.controller.updateEnabledStudies).toBeFunction();
      expect(this.controller.getEnabledStudiesPanelHeader).toBeFunction();
    });

  });

});
